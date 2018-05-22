package com.andreaak.note.fileSystemItems;

public class FileItem extends FileSystemItem implements Comparable<FileItem> {
    private String name;
    private String data;
    private String date;
    private String path;

    public FileItem(String name, String data, String date, String path, ItemType type) {
        super(type);
        this.name = name;
        this.data = data;
        this.date = date;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

    public int compareTo(FileItem o) {
        if (this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}
