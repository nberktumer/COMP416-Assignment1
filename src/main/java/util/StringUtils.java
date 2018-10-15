package util;

public class StringUtils {
    private StringUtils() {
    }

    /**
     * Converts byte array into string
     *
     * @param arr Byte array to be converted to string
     * @return String value of the given byte array
     */
    public static String byteArrayToString(byte[] arr) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            stringBuilder.append(Integer.toString((arr[i] & 0xff) + 0x100, 16).substring(1));
        }

        return stringBuilder.toString();
    }
}
