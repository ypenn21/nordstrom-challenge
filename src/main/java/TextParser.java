package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yannipeng on 5/15/18.
 * Quick dirty solution
 */
public class TextParser {

    //private static final String FOLDER = "/Users/yannipeng/Downloads/Coding-Challenge";
    private static final String PATTERN_FILE = "shoe[0-9]{0,1000}.txt";
    private static final String PATTERN_HEEL_STYLE = "-\\s.+(heel)[\\s\\.]*";
    private static final String PATTERN_HEEL_HEIGHT = "(-\\s\\D+)([+ -]?[0-9]{1,2}([.]+[0-9]{0,2})?)\".+";
    private static final String PATTERN_TOE_STYLE = "-\\s.+(toe)";
    private static final String PATTERN_MATERIAL = "(Materials)|(materials)";
    private static final String PATTERN_FEATURE = "-\\s.+(padded\\sfootbed)";

    private static final String TOE_STYLE = "Toe_Style";
    private static final String HEEL_STYLE = "Heel_Style";
    private static final String HEEL_HEIGHT = "Heel_Height";
    private static final String MATERIAL = "Material";
    private static final String FEATURE = "Feature";

    // final Pattern [] patterns = {heel, toe, material, feature, heelHeight};
    private static final int TOE_STYLE_INDEX = 1;
    private static final int HEEL_STYLE_INDEX = 0;
    private static final int HEEL_HEIGHT_INDEX = 4;
    private static final int MATERIAL_INDEX = 2;
    private static final int FEATURE_INDEX = 3;
    private static final String [] ATTRIBUTES_SEQUENCE = {HEEL_STYLE, TOE_STYLE, MATERIAL, FEATURE, HEEL_HEIGHT};

    public static void main(String[] args) {

        System.out.println("Enter The Absolute Path of Coding-Challenge Folder");
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        File directory = new File(reader.nextLine()); // I put my files /Users/yannipeng/Downloads/Coding-Challenge
        File[] contents = directory.listFiles();

        BufferedReader br = null;
        FileReader fr = null;

        try {
            final Pattern [] patterns = getPatternArray();

            for(File file : contents) { // looping through files in folder
                if(file.getName().matches(PATTERN_FILE)) {
                    System.out.println();
                    System.out.println(file.getName().replaceFirst(".txt", "")+":");
                    fr = new FileReader(file.getAbsolutePath());
                    br = new BufferedReader(fr);
                    String sCurrentLine;
                    boolean isMaterial = false;
                    Map attributes = new HashMap<String, String>();
                    while ((sCurrentLine = br.readLine()) != null) { // looping through lines in file that match PATTERN_FILE
                        String cleanLine = sCurrentLine.trim();
                        Matcher materialMatch = patterns[MATERIAL_INDEX].matcher(cleanLine);
                        if (materialMatch.find()) { // if match material then we assume next line is the material value
                            isMaterial = true;
                            continue;
                            //System.out.println(m.matches());
                        }
                        if(cleanLine.equals("") || (cleanLine.charAt(0) != '-' && !isMaterial))
                            continue;
                        // for simplicity i know the index of each of the pattern. ex index 0 is heel
                        for(int i =0 ; i < patterns.length ; i++ ) {
                            Matcher m = patterns[i].matcher(cleanLine);

                            if (isMaterial){ //Result Material: "
//                                System.out.print("Material : ");
                                attributes.put( MATERIAL, capitalizeWord(getMaterial(cleanLine)) );
                                isMaterial=false;
                                break;
                            }

                            if(i==HEEL_STYLE_INDEX && m.find() && !cleanLine.matches(PATTERN_HEEL_HEIGHT)){ //"Result Heel Style: "
                                String style = cleanLine;
//                                System.out.print("Heel_Style : ");
                                if ( cleanLine.contains("Stiletto")) {
                                    style = "Stiletto";
                                }
                                attributes.put(HEEL_STYLE, capitalizeWord(style));
                                //System.out.println(capitalizeWord(style));
                                break;
                            }

                            if (i==TOE_STYLE_INDEX && m.find()){ //Toe_Style:
                                attributes.put(TOE_STYLE, capitalizeWord(cleanLine));
//                                System.out.println("Toe_Style : "+capitalizeWord(cleanLine));
                                break;
                            }

                            if (i==FEATURE_INDEX && m.find()){ //Result Feature:
                                attributes.put(FEATURE, "Padding");
                                //System.out.println("Feature : Padding");
                                break;
                            }

                            //                            Heel 1”-2” = Low Heel
                            //                            Heel 2”-3” = Mid Heel
                            //                            Heel 3”-4” = High Heel
                            if(i==HEEL_HEIGHT_INDEX && m.find()){ //Result Heel:
                                float inches =Float.parseFloat(m.group(2));
                                attributes.put( HEEL_HEIGHT, capitalizeWord(getHeelHeight( inches )) );
                                break;
                            }

                            if (m.find() && !isMaterial) { // Result of anything left over:
                                System.out.println(" Missed this attribute "+capitalizeWord(cleanLine));
                                break;
                            }
                        }
                    }
                    printAttributes(attributes);
                }
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }

    }

    private static String getHeelHeight(float inches){
        //System.out.print("Heel_Height : ");
        String heelHeight = "";
        if(inches<=2.0){
            heelHeight = "Low Heel";
        } else if(inches<=3.0){
            heelHeight = "Mid Heel";
        } else {
            heelHeight = "High Heel";
        }
        return heelHeight;
    }

    private static void printAttributes(Map attributes){
        for(String attribute : ATTRIBUTES_SEQUENCE) {
            if(attributes.get(attribute) !=null )
                System.out.println( attribute + " : "+attributes.get(attribute) );
        }
    }

    private static String getMaterial(String line){
        String material;
        if(line.matches(".*(Synthetic upper).*")){
            material = "Manmade";
        } else if(line.matches(".*(Fabric upper).*") || line.matches(".*(suede upper).*")){
            material = "Suede";
        } else {
            material = "Canvas";
        }
        return material;
    }

    private static Pattern [] getPatternArray(){
        final Pattern heelHeight = Pattern.compile(PATTERN_HEEL_HEIGHT);
        final Pattern heel = Pattern.compile(PATTERN_HEEL_STYLE);
        //            if (heel.matcher("- Stiletto heel").find()) {
//                System.out.println("Found!!!!");
//            }
        final Pattern toe = Pattern.compile(PATTERN_TOE_STYLE);
        final Pattern material = Pattern.compile(PATTERN_MATERIAL);
        final Pattern feature = Pattern.compile(PATTERN_FEATURE);
        final Pattern [] patterns = {heel, toe, material, feature, heelHeight};
        return patterns;
    }

    private static String capitalizeWord(String str){
        str = str.replace("-", "").trim();
        String words[]=str.split("\\s");
        StringBuffer stringBuffer = new StringBuffer();
        for(String w:words){
            String first=w.substring(0,1);
            String afterfirst=w.substring(1);
            stringBuffer.append(first.toUpperCase()+afterfirst+" ");
        }
        return stringBuffer.toString().trim();
    }

}
