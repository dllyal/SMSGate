package com.dllyal.cmpp.entity;


import com.dllyal.cmpp.utils.CmppUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author
 */
public class CmppActiveTest extends CmppMessageHeader {

    public CmppActiveTest() {
        this.setTotal_Length(12);
        this.setCommand_Id(CmppDefine.CMPP_ACTIVE_TEST);
        this.setSequence_Id(CmppUtils.getSequence());
    }

    /**
     * 实现类必须自定义对象序列化
     *
     * @return
     */
    @Override
    public byte[] toByteArray() {
        ByteBuf buf = Unpooled.buffer(12);
        buf.writeInt(this.getTotal_Length());
        buf.writeInt(this.getCommand_Id());
        buf.writeInt(this.getSequence_Id());
        return buf.array();
    }
}
