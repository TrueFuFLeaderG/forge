package forge.net;

import forge.GuiBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mapdb.elsa.ElsaObjectOutputStream;

import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CustomObjectEncoder extends MessageToByteEncoder<Serializable> {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    public CustomObjectEncoder() {
    }

    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();
        ByteBufOutputStream bout = new ByteBufOutputStream(out);

        if (GuiBase.hasPropertyConfig()){
            ElsaObjectOutputStream oout = null;
            try {
                bout.write(LENGTH_PLACEHOLDER);
                oout = new ElsaObjectOutputStream(bout);
                oout.writeObject(msg);
                oout.flush();
            } finally {
                if (oout != null) {
                    oout.close();
                } else {
                    bout.close();
                }
            }
        } else {
            ObjectOutputStream oout = null;
            try {
                bout.write(LENGTH_PLACEHOLDER);
                oout = new ObjectOutputStream(bout);
                oout.writeObject(msg);
                oout.flush();
            } finally {
                if (oout != null) {
                    oout.close();
                } else {
                    bout.close();
                }
            }
        }

        int endIdx = out.writerIndex();
        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}