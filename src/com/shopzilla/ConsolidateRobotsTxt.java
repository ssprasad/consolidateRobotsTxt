/**
 * Copyright (C) 2004 - 2013 Shopzilla, Inc. 
 * All rights reserved. Unauthorized disclosure or distribution is prohibited.
 */
package com.shopzilla;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility Class to clean up duplicate entries in robots.txt
 * 
 * @author sshekhar
 * 
 * @since 07-May-2013
 */
public class ConsolidateRobotsTxt {

    private static final String ROBOTS_FILE_NAME = "robots.txt";
    private static final String USER_AGENT_PREFIX = "User-agent:";
    private static final String DISALLOW_PREFIX = "Disallow:";
    private static final String ALLOW_PREFIX = "Allow:";
    private static final String SITEMAP_PREFIX = "Sitemap:";
    private static final String ALL_ROBOTS = USER_AGENT_PREFIX + " *";
    private static final String COMMENT_PREFIX = "#";

    private static Map<String, List<String>> bots = new LinkedHashMap<String, List<String>>();
    private static String currentBot = null;

    private static final Logger LOGGER = Logger.getLogger(ConsolidateRobotsTxt.class.getName());

    private static void readRobotsFile() {
        BufferedReader br = null;
        try {
            String currentLine;
            br = new BufferedReader(new FileReader(ROBOTS_FILE_NAME));
            while ((currentLine = br.readLine()) != null) {
                processLine(currentLine);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    private static void processLine(String currentLine) {
        if (currentLine.startsWith(USER_AGENT_PREFIX)) {
            currentBot = currentLine;
            bots.put(currentLine, new LinkedList<String>());
        } else {
            List<String> disallowedAllRobots = bots.get(ALL_ROBOTS);
            if (currentLine.trim().isEmpty() || currentLine.startsWith(COMMENT_PREFIX)
                    || !disallowedAllRobots.contains(currentLine)) {
                addToDisallow(currentBot, currentLine);
            }
        }
    }

    private static void addToDisallow(String currentBot, String line) {
        if (currentBot == null) {
            bots.put(line, new LinkedList<String>());
            return;
        }
        List<String> disallowedList = bots.get(currentBot);
        disallowedList.add(line);
        bots.put(currentBot, disallowedList);
    }

    private static void createRobotsFile() {
        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter(ROBOTS_FILE_NAME);
            out = new BufferedWriter(fstream);
            writeContent(out);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    private static void writeContent(BufferedWriter out) throws IOException {
        for (String bot : bots.keySet()) {
            List<String> disallowedList = bots.get(bot);
            if (bot.startsWith(USER_AGENT_PREFIX) && !disallowedList.isEmpty()) {
                boolean firstValidLineFoundToWrite = false;
                for (String line : disallowedList) {
                    if ((line.startsWith(DISALLOW_PREFIX) || line.startsWith(ALLOW_PREFIX) || line.startsWith(SITEMAP_PREFIX))
                            && !firstValidLineFoundToWrite) {
                        firstValidLineFoundToWrite = true;
                        // Make the User-agent entry here
                        out.write(bot);
                        out.newLine();
                    }

                    // Disallow Prefix should come after user agent line.
                    if (firstValidLineFoundToWrite) {
                        out.write(line);
                        out.newLine();
                    }
                }

                out.newLine();
            }
        }

    }

    private static void generateConsolidatedRobotsFile() {
        readRobotsFile();
        LOGGER.log(Level.INFO, "***DONE Reading of Robots.txt file***");
        createRobotsFile();
        LOGGER.log(Level.INFO, "***DONE Creating of Robots.txt file***");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "***Starting Consolidation of Robots.txt file***");
        generateConsolidatedRobotsFile();
        LOGGER.log(Level.INFO, "***Finished Consolidation of Robots.txt file***");
    }

}