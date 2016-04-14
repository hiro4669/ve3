package ve3.util;

import java.io.ByteArrayOutputStream;

public class BCDUtil {
    public static final int[] BCD2INT = new int[]{
        0,1,2,3,4,5,6,7,8,9,0,0,0,0,0,0,
        10,11,12,13,14,15,16,17,18,19,0,0,1,1,0,0,
        20,21,22,23,24,25,26,27,28,29,0,0,2,2,0,0,
        30,31,32,33,34,35,36,37,38,39,0,0,3,3,0,0,
        40,41,42,43,44,45,46,47,48,49,0,0,4,4,0,0,
        50,51,52,53,54,55,56,57,58,59,0,0,5,5,0,0,
        60,61,62,63,64,65,66,67,68,69,0,0,6,6,0,0,
        70,71,72,73,74,75,76,77,78,79,0,0,7,7,0,0,
        80,81,82,83,84,85,86,87,88,89,0,0,8,8,0,0,
        90,91,92,93,94,95,96,97,98,99,0,0,9,9,0,0,
    };
     
    public static final byte[] INT2BCD = new byte[]{
        (byte)0x0,(byte)0x1,(byte)0x2,(byte)0x3,(byte)0x4,
        (byte)0x5,(byte)0x6,(byte)0x7,(byte)0x8,(byte)0x9,
        (byte)0x10,(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,
        (byte)0x15,(byte)0x16,(byte)0x17,(byte)0x18,(byte)0x19,
        (byte)0x20,(byte)0x21,(byte)0x22,(byte)0x23,(byte)0x24,
        (byte)0x25,(byte)0x26,(byte)0x27,(byte)0x28,(byte)0x29,
        (byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,
        (byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
        (byte)0x40,(byte)0x41,(byte)0x42,(byte)0x43,(byte)0x44,
        (byte)0x45,(byte)0x46,(byte)0x47,(byte)0x48,(byte)0x49,
        (byte)0x50,(byte)0x51,(byte)0x52,(byte)0x53,(byte)0x54,
        (byte)0x55,(byte)0x56,(byte)0x57,(byte)0x58,(byte)0x59,
        (byte)0x60,(byte)0x61,(byte)0x62,(byte)0x63,(byte)0x64,
        (byte)0x65,(byte)0x66,(byte)0x67,(byte)0x68,(byte)0x69,
        (byte)0x70,(byte)0x71,(byte)0x72,(byte)0x73,(byte)0x74,
        (byte)0x75,(byte)0x76,(byte)0x77,(byte)0x78,(byte)0x79,
        (byte)0x80,(byte)0x81,(byte)0x82,(byte)0x83,(byte)0x84,
        (byte)0x85,(byte)0x86,(byte)0x87,(byte)0x88,(byte)0x89,
        (byte)0x90,(byte)0x91,(byte)0x92,(byte)0x93,(byte)0x94,
        (byte)0x95,(byte)0x96,(byte)0x97,(byte)0x98,(byte)0x99
    };

	private static int getLength(byte[] data) {
		int len;
		if ((len = data.length) > 0) {
			len *= 2;
			len += (((data[0] >> 4) & 0xf) == 0) ? -1 : 0;
		}
		return len;		
	}

	public static byte[] convert(byte[] data, int length) {
		int packlen = getLength(data) - 1;
		if (packlen == length) {
			return data;
		} else {
			int newlen = length / 2 + 1;
			boolean even = (length % 2) == 0;
			byte[] newdata = new byte[newlen];
			int pos = newlen - 1;
			for (int i = data.length - 1; i >= 0; --i, --pos) {
				if (pos < 0) break;
				newdata[pos] = data[i];				
			}
			if (even) {
				newdata[0] &= 0xf;
			}
			return newdata;
		}
	}
     
    public static void main(String[] args) {
		int original = -1234;
		System.out.println("original = " + original);
		byte[] data = int2bcd(original);
		byte[] newdata = convert(data, 1);
		for (int i = 0; i < newdata.length; ++i) {
			System.out.printf("%02x ", newdata[i]);
		}
		int newnum = bcd2int(newdata);
		System.out.println("\nnewnum = " + newnum);
    }
     
    public static int bcd2int(byte[] bcd){
        int p = 0;
        int cnt;
        for (cnt = 0; cnt < (bcd.length - 1); cnt++) {
            p = p * 100 + BCD2INT[bcd[cnt] & 0xff];
        }
        p = (p * 10 + BCD2INT[bcd[cnt] & 0xff]) * ((bcd[cnt] & 0x0f) == 13 ? -1 : 1);
        return p;        
    }
     
    public static byte[] int2bcd(int intNum){
        int p = intNum;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();         
        if (p < 0) {
			p *= -1;
            bout.write((p % 10) * 16 + 13);
        } else {
            bout.write((p % 10) * 16 + 12);            
        }
        p /= 10;         
        while (p > 0) {
            bout.write(INT2BCD[p % 100]);
			p /= 100;
        }
        return reverse(bout.toByteArray());
    }
     
    public static byte[] reverse(byte[] src) {         
        int left = 0;
        int right = src.length - 1;
        byte tmp;
         
        while(left < right) {
            tmp = src[right];
            src[right] = src[left];
            src[left] = tmp;
             
            left += 1;
            right -= 1;
        }         
        return src;
    }
}

