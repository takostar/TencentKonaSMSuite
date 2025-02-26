/*
 * Copyright (C) 2022, 2023, THL A29 Limited, a Tencent company. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation. THL A29 Limited designates
 * this particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.tencent.kona.crypto.spec;

import javax.crypto.spec.SecretKeySpec;

/**
 * @deprecated Use <code>SecretKeySpec(key, "SM4")</code> instead
 */
@Deprecated
public class SM4KeySpec extends SecretKeySpec {

    private static final long serialVersionUID = -621469601839652916L;

    public SM4KeySpec(byte[] key, int offset, int length) {
        super(key, offset, length, "SM4");
    }

    public SM4KeySpec(byte[] key) {
        this(key, 0, key.length);
    }

    public SM4KeySpec(byte[] key, int offset) {
        this(key, offset, key.length - offset);
    }
}
