package CommonUtils;

import java.io.Serializable;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
public class InitPackage implements Serializable {
    private long fileSize;
    private int lastPackageSize;
    private String fileName;
    private long packageCount;


    public InitPackage(long fileSize, int lastPackageSize, String fileName, long packageCount)
    {
        this.fileSize = fileSize;
        this.lastPackageSize = lastPackageSize;

        this.fileName = fileName;
        this.packageCount = packageCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InitPackage that = (InitPackage) o;

        if (fileSize != that.fileSize) return false;
        if (packageCount != that.packageCount) return false;
        return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (fileSize ^ (fileSize >>> 32));
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (int) (packageCount ^ (packageCount >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "InitPackage{" +
                "fileSize=" + fileSize +
                ", fileName='" + fileName + '\'' +
                ", packageCount=" + packageCount +
                '}';
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getLastPackageSize() {
        return lastPackageSize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getPackageCount() {
        return packageCount;
    }
}
