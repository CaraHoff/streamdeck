package de.carahoff.streamdeck.util;

public class ImageData {
    private byte[] image;
    private int pageSize;
    
    public ImageData(byte[] image, int pageSize) {
        this.image = image;
        this.pageSize = pageSize;
    }
    
    public byte[] page(int pageIndex) {
        int offset = pageIndex * pageSize;
        if (offset >= image.length) {
            return new byte[]{};
        }

        int length = pageLength(pageIndex);
        if (offset + length > image.length) {
            length = image.length - offset;
        }

        byte[] pageData = new byte[length];
        System.arraycopy(image, offset, pageData, 0, length);

        return pageData;
    }
    private int pageLength(int pageIndex) {
        int remaining = image.length - (pageIndex * pageSize);
        if (remaining > pageSize) {
            return pageSize;
        }
        if (remaining > 0) {
            return remaining;
        }
        return 0;
    }

    public int pageCount() {
        int count = image.length / pageSize;
        if (image.length % pageSize != 0) {
            return count + 1;
        }
        return count;
    }

    public int length() {
        return image.length;
    }
}
