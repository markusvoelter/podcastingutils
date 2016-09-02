package net.omegataupodcast.hasher;

import java.io.*;
import java.math.BigInteger;
import java.rmi.server.ExportException;
import java.security.MessageDigest;
import java.util.*;

public class Main {

    public static void main(String[] args) {
	    String subjectDir = args[0];
        List<File> files = findFiles(subjectDir);
        String wd = System.getProperty("user.dir");

        System.err.println("Processing Dir:          "+subjectDir);
        System.err.println("No of files:             "+files.size());
        System.err.println("Working Dir:             "+wd);


        Map<String, String> oldHashData = loadOldHashData(wd);

        System.err.print("Hashing ...");
        Map<String, String> newHashes = new HashMap<String, String>();
        for (File f: files) {
            String abs = f.getAbsolutePath();
            String rel = abs.substring(subjectDir.length());
            newHashes.put(rel,hash(f));
        }
        System.err.println(" Done.");

        save(subjectDir, wd, newHashes);

        System.err.println("Diffing: ");
        for (String k: newHashes.keySet()) {
            String newFile = k;
            if (newFile.startsWith("/webseiten/wp-content/cache/")) continue;
            if (!oldHashData.containsKey(k)) {
                System.err.println("New File: "+k);
            } else {
                String newHash = newHashes.get(k);
                String oldHash = oldHashData.get(k);
                if ( !newHash.equals(oldHash)) {
                    System.err.println("Different Hash: "+k);
                }
            }
        }
        System.err.println("Done.");


    }

    private static Map<String, String> loadOldHashData(String workingDir) {
        Map<String, String> res = new HashMap<String, String>();
        String dataPath = makeDataPath(workingDir);
        File dataDir = new File(dataPath);
        int last = dataDir.listFiles().length;
        if (last == 0) return null;
        String name = makeDataFileName(makeDataPath(workingDir), last);
        System.err.println("Loading old Hashes from: " + name);
        try {
            FileReader fileReader = new FileReader(new File(name));
            BufferedReader br = new BufferedReader(fileReader);
            String line = null;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ";");
                res.put(st.nextToken(),st.nextToken());
            }
        } catch (Exception ex ) {
            ex.printStackTrace();
        }
        return res;
    }

    private static String makeDataPath(String workingDir) {
        return workingDir + "/data";
    }


    private static void save(String subjectDir, String workingDir, Map<String, String> hashData) {
        String dataPath = makeDataPath(workingDir);
        File dataDir = new File(dataPath);
        int idx = dataDir.listFiles().length + 1;
        String saveName = makeDataFileName(dataPath, idx);
        System.err.println("Writing to file:         "+saveName);
        try {
            FileWriter writer = new FileWriter(saveName);
            for (String k: hashData.keySet()) {
                writer.write(k+";"+hashData.get(k)+"\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String makeDataFileName(String dataPath, int idx) {
        return dataPath+"/data_"+idx+".txt";
    }


    private static String hash(File f) {
        String hash = "<invalid>";
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] raw = md5.digest(data);
            BigInteger bigInt = new BigInteger(1,raw);
            hash = bigInt.toString(16);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }

    private static List<File> findFiles(String dir) {
        List<File> collected = new ArrayList<File>();
        findFiles(new File(dir),collected);
        return collected;
    }

    private static void findFiles(File dir, List<File> collector) {
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles();
        for (File f: files) {
            if (f.isDirectory()) findFiles(f,collector);
            else collector.add(f);
        }
    }
}
