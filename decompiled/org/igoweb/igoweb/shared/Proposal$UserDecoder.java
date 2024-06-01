/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInput;
import java.io.IOException;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.User;

public static interface Proposal.UserDecoder<UserT extends User> {
    public UserT getUser(DataInput var1) throws IOException, Proposal.NoSuchUserException;
}
