package eDLineEditor;

import java.util.List;
import java.util.Scanner;

public class SaveArea {
    private String command;
    static String defaultFilename;
    static int currentLine;
    static State state=null;
    static List<List<String>> saveList;
    static List<Integer> lineList;
    static  int undoCounter=0;
    static List<String> memory;
    static Scanner sc;
    //todo 缓存区修改
}
