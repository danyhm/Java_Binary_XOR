package ir.doorpa;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Class to do XOR operations on different types of inputs and return the results as needed.
 * the XOR size must be defined and the key value must be set before any operation can take place
 * @see XorSize
 * @author Danial
 * */
public class Xor {
    /**
     * Public enum that holds valid values for XOR value size
     */
    public enum XorSize{
        XOR_SIZE_8_BIT(1),
        XOR_SIZE_16_BIT(2),
        XOR_SIZE_32_BIT(4),
        XOR_SIZE_64_BIT(8),
        XOR_SIZE_128_BIT(16);

        private final int value;

        XorSize(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }

    /**
     * Xor size
     */
    private XorSize xorSize = XorSize.XOR_SIZE_8_BIT;

    /**
     * Xor Value
     */
    private byte[] xorValue;

    /**
     * sets the XOR size and recalculates the value accordingly
     * @param xorSize the new XOR size
     * */
    public void setXorSize(XorSize xorSize) {
        recalculateSizeValue(xorSize, getXorValue());
    }

    /**
     * sets the XOR value and recalculates the parameters.
     * @param xorValue the new value as an array of bytes.
     * @link recalculateSizeValue
     * */
    public void setXorValue(byte[] xorValue) {
        recalculateSizeValue(getXorSize(), xorValue);
    }

    /**
     * sets the XOR size and value
     * @note this function must be called before any operation can take place
     * @param xorSize the size to set for the operation which must be a value of @see {@link XorSize}
     * @param value the byte array with the corresponding size with xorSize. if the size is less it will be filled
     *              with zeros, and if the size is more the rest is trimmed.
     */
    private void recalculateSizeValue(XorSize xorSize, byte[] value){
        if (value == null){
            value  = new byte[1];
        }

        int srcsize = value.length;
        int destsize = xorSize.getValue();
        this.xorSize = xorSize;

        /* calculate correct value according to xorsize */
        if(destsize != srcsize){
            /* alignment is needed */
            ByteBuffer buffer = ByteBuffer.allocate(destsize);
            if(srcsize > destsize){
                buffer.put(value,0, destsize);
            }else{ /* srcsize < destsize */
                buffer.position(destsize - srcsize);
                buffer.put(value);
                /* the rest is already filled with zero */
            }
            xorValue = buffer.array();
        }else {
            /* copy value directly */
            xorValue = value.clone();
        }
    }

    /**
     * returns the current Xor Size
     * @return the current size for XOR operations.
     * */
    public XorSize getXorSize(){
        return xorSize;
    }

    /**
     * returns the current Xor value
     * @return the current value for XOR operations
     * */
    public byte[] getXorValue(){
        return xorValue;
    }


    /*
    * if file or buffer size is not aligned with xor key value ? padding must be added !
    * */

    /**
     * starts to Xor the given file with the current key and returns the result as a new file created in the same directory.
     * @param src the source of the original file to be XORed.
     * @return the File that has been XORed and already written in the same directory as the original file.
     * */
    public File doXor(File src){
        File dest;

        /* get the name of the source file */
        String sourceName = src.getName();

        /* create a file with the same name and _xor_size_value*/
        dest = new File(src.getParent(), "XOR_Size" + xorSize.getValue() + "_Key" + keyAsHex().replace(" ","") +"_" + sourceName);

        if(dest.exists()){
            /* delete it */
            dest.delete();
        }

        try(
            RandomAccessFile srcReader  = new RandomAccessFile(src,"r");
            RandomAccessFile destWriter = new RandomAccessFile(dest,"rw");
            FileChannel inChannel = srcReader.getChannel();
            FileChannel outChannel = destWriter.getChannel()
        ){
            int fileSize = (int)inChannel.size();
            ByteBuffer inbuffer = ByteBuffer.allocate( fileSize );
            ByteBuffer outbuffer = ByteBuffer.allocate( fileSize + neededPadding(fileSize));
            inChannel.read(inbuffer);
            //buffer.rewind();
            inbuffer.flip();
            /* start XORing */
            /* create a byte big enough according to XOR size */
            byte[] tempbyte = new byte[xorSize.getValue()];
            /* do the actual XOR */
            while(inbuffer.hasRemaining()){
                inbuffer.get(tempbyte);
                xor(tempbyte,xorValue);
                outbuffer.put(tempbyte);

            }
            /* flip buffer and get ready to write to file */
            outbuffer.flip();
            /* write to file */
            outChannel.write(outbuffer);
        }catch (IOException e){

        }

        return dest;
    }

    /**
     * return the value of XOR key as a HEX representation in String
     * @return the String which holds the representation
     * */
    public String keyAsHex(){
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : xorValue) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    /**
     * return the value of XOR key as a Binary representation in String
     * @return the String which holds the representation
     * */
    public String keyAsBin(){
        StringBuilder sb = new StringBuilder();
        sb.append("0b");
        for (byte b : xorValue) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return sb.toString();
    }

    /**
     * return the value of XOR key as an Integer representation in String
     * @return the String which holds the representation
     * */
    public String keyAsInt(){
        BigInteger bigInteger = new BigInteger(xorValue);
        //BigDecimal bigDecimal = new BigDecimal(bigInteger);
        return bigInteger.toString();
    }

    /**
     * XOR the 2 given array of bytes and stores the result in first one.
     * @param srcDest the source and Destination of the resulting XOR
     * @param key the key to XOR with
     *
     * */
    private void xor(byte[] srcDest, byte[] key){
        int i = srcDest.length;
        for (int j = 0; j < i ; j++) {
            srcDest[j] ^= key[j];
        }

    }

    /**
     * simple method to calculate if a file of the given size needs padding according to the XOR size
     * @param fileSize the file size to calculate the padding for
     * @return a value between 1 up to (MAX_XOR size - 1) if a padding is needed or zero if no padding is needed.
     * */
    public int neededPadding(int fileSize){
        int i = xorSize.getValue();
        return fileSize % i;
    }

}
