/*
 * Copyright (c) 2014 "Kaazing Corporation," (www.kaazing.com)
 *
 * This file is part of Robot.
 *
 * Robot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.kaazing.k3po.driver.behavior.handler.codec;

import static java.util.Objects.requireNonNull;
import static org.jboss.netty.buffer.ChannelBuffers.EMPTY_BUFFER;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.kaazing.k3po.driver.behavior.ScriptProgressException;
import org.kaazing.k3po.lang.RegionInfo;

public abstract class MessageDecoder {

    private final RegionInfo regionInfo;
    private ChannelBuffer cumulation;

    protected MessageDecoder(RegionInfo regionInfo) {
        this.regionInfo = requireNonNull(regionInfo);
    }

    public ChannelBuffer decodeLast(ChannelBuffer buffer) throws Exception {
        return decode0(buffer, true);
    }

    // Returns the ChannelBuffer that should be passed on to the next
    // handler the pipeline, or null if more data is needed by the
    // decoder.
    public ChannelBuffer decode(ChannelBuffer buffer) throws Exception {
        return decode0(buffer, false);
    }

    protected RegionInfo getRegionInfo() {
        return regionInfo;
    }

    private ChannelBuffer decode0(ChannelBuffer buffer, boolean isLast) throws Exception {
        try {
            // If we don't have a cumulation buffer yet, create it
            if (cumulation == null) {
                cumulation = ChannelBuffers.dynamicBuffer();
            }

            // Write the input bytes in the cumulation buffer
            cumulation.writeBytes(buffer);

            Object decoded;
            if (isLast) {
                decoded = decodeBufferLast(cumulation);
            } else {
                decoded = decodeBuffer(cumulation);
            }

            if (decoded == null) {
                // Not enough data yet, keeping accumulating more (unless last)
                return null;
            }

            ChannelBuffer remaining = EMPTY_BUFFER;

            if (cumulation.readable()) {
                // The decoder did not consume all of our accumulated bytes; create
                // the ChannelBuffer to pass on.
                remaining = cumulation.readBytes(cumulation.readableBytes());
            }

            // Let the VM know we're done with the cumulation buffer
            cumulation = null;

            return remaining;
        }
        catch (ScriptProgressException e) {
            // clean up on failure to prevent side-effects when re-using decoder
            cumulation = null;
            throw e;
        }
    }

    protected ChannelBuffer createCumulationBuffer(ChannelHandlerContext ctx) {
        return ChannelBuffers.dynamicBuffer();
    }

    protected Object decodeBufferLast(ChannelBuffer buffer) throws Exception {
        // by default, no distinct behavior between last and non-last
        return decodeBuffer(buffer);
    }

    protected abstract Object decodeBuffer(ChannelBuffer buffer) throws Exception;
}