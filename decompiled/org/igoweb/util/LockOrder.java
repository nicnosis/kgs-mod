/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class LockOrder {
    private static final ThreadLocal<ArrayList<Object>> locksHeld = new ThreadLocal();
    private static final Object mapWriteLock = new Object();
    private static final Map<WeakHashRef, LockOrder> objectToOrder = Collections.synchronizedMap(new HashMap());
    private static volatile HashMap<Class<?>, LockOrder> classToOrder = new HashMap();
    private static final ReferenceQueue<Object> refQ = new ReferenceQueue();
    private final String name;
    private volatile HashSet<LockOrder> innerOrders = new HashSet();
    private final HashSet<LockOrder> outerOrders = new HashSet();

    public LockOrder(String newName) {
        this.name = newName;
    }

    public LockOrder(Class<?> klass) {
        this.name = klass.getName();
        this.orderFor(klass);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean addInnerOrder(LockOrder newInner) {
        if (newInner == this) {
            throw new IllegalArgumentException("Order " + this + " cannot be inside itself");
        }
        Object object = mapWriteLock;
        synchronized (object) {
            if (this.innerOrders.contains(newInner)) {
                return true;
            }
            HashSet<LockOrder> newInners = new HashSet<LockOrder>(this.innerOrders);
            if (newInner.innerOrders.contains(this)) {
                throw new IllegalStateException("Loop found! Order " + this + " in inside and outside " + newInner);
            }
            newInners.add(newInner);
            newInner.outerOrders.add(this);
            newInner.outerOrders.addAll(this.outerOrders);
            for (LockOrder inner : newInner.innerOrders) {
                if (inner == this) {
                    throw new RuntimeException("Bug in lock order class");
                }
                newInners.add(inner);
                inner.outerOrders.add(this);
                inner.outerOrders.addAll(this.outerOrders);
            }
            for (LockOrder outer : this.outerOrders) {
                if (outer == this) {
                    throw new RuntimeException("Bug in lock order class");
                }
                HashSet<LockOrder> newOuterInners = new HashSet<LockOrder>(outer.innerOrders);
                newOuterInners.addAll(newInners);
                outer.innerOrders = newOuterInners;
            }
            this.innerOrders = newInners;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean orderFor(Class<?> lockClass) {
        Object object = mapWriteLock;
        synchronized (object) {
            HashMap newClassToOrderMap = new HashMap(classToOrder);
            LockOrder oldOrder = newClassToOrderMap.put(lockClass, this);
            if (oldOrder != null) {
                throw new IllegalStateException("Class " + lockClass + " already has lock order of " + oldOrder + "; cannot add " + this);
            }
            classToOrder = newClassToOrderMap;
        }
        return true;
    }

    public boolean orderFor(Object lock) {
        LockOrder.cleanRefQ();
        if (classToOrder.containsKey(lock.getClass())) {
            throw new IllegalStateException("Class of " + LockOrder.shortName(lock) + " already has lock order of " + classToOrder.get(lock.getClass()) + "; cannot add instance order " + this);
        }
        LockOrder prevOrder = objectToOrder.put(new WeakHashRef(lock, true), this);
        if (prevOrder != null) {
            throw new IllegalStateException("Object " + LockOrder.shortName(lock) + " already has lock order of " + prevOrder + "; cannot add " + this);
        }
        return true;
    }

    public static LockOrder getOrder(Object lock) {
        LockOrder result = classToOrder.get(lock.getClass());
        if (result == null && (result = objectToOrder.get(new WeakHashRef(lock, false))) == null) {
            throw new IllegalArgumentException("No lock order specified for " + LockOrder.shortName(lock));
        }
        return result;
    }

    public static boolean hasOrder(Class<?> klass) {
        return classToOrder.containsKey(klass);
    }

    public static boolean testAcquire(Object newLock) {
        if (Thread.holdsLock(newLock)) {
            return true;
        }
        LockOrder.testOrdering(newLock, "acquire").add(newLock);
        return true;
    }

    public static boolean testWait(Object waitLock) {
        if (!Thread.holdsLock(waitLock)) {
            throw new IllegalStateException("Cannot wait on lock " + LockOrder.shortName(waitLock) + " because you are not synchronized on it.");
        }
        LockOrder.testOrdering(waitLock, "wait on");
        return true;
    }

    private static ArrayList<Object> testOrdering(Object lock, String action) {
        ArrayList<Object> oldLocks = locksHeld.get();
        if (oldLocks == null) {
            oldLocks = new ArrayList();
            locksHeld.set(oldLocks);
            return oldLocks;
        }
        LockOrder order = LockOrder.getOrder(lock);
        Iterator<Object> iter = oldLocks.iterator();
        while (iter.hasNext()) {
            Object oldLock = iter.next();
            if (Thread.holdsLock(oldLock)) {
                if (oldLock == lock) continue;
                LockOrder oldOrder = LockOrder.getOrder(oldLock);
                if (oldOrder.innerOrders.contains(order)) continue;
                throw new IllegalStateException("Attempt to " + action + " lock " + LockOrder.shortName(lock) + " (order " + order + ") inside of lock " + LockOrder.shortName(oldLock) + " (order " + oldOrder + ") violates lock ordering.");
            }
            iter.remove();
        }
        return oldLocks;
    }

    public String toString() {
        return "LockOrder[" + this.name + "]";
    }

    private static void cleanRefQ() {
        Reference<Object> ref;
        while ((ref = refQ.poll()) != null) {
            objectToOrder.remove(ref);
        }
        return;
    }

    private static String shortName(Object obj) {
        String result = obj.toString();
        return result.length() > 100 ? result.substring(0, 100) + "..." : result;
    }

    private static class WeakHashRef
    extends WeakReference<Object> {
        private final int objectHashCode;

        public WeakHashRef(Object o, boolean inRefQ) {
            super(o, inRefQ ? refQ : null);
            this.objectHashCode = System.identityHashCode(o);
        }

        public int hashCode() {
            return this.objectHashCode;
        }

        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            WeakHashRef peer = (WeakHashRef)o;
            return this.objectHashCode == peer.objectHashCode && this.get() == peer.get();
        }
    }
}
