/**
 * Copyright (C) 2015 Paweł Marynowski
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version. Additionally this file is subject to the
 * "Classpath" exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package org.wikimedia.commons.movecat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.wikipedia.Wiki;

/**
 * Main class
 *
 */
public class Main {

  static boolean TEST = false;

  static Wiki wiki = new Wiki("commons.wikimedia.org");
  static String user;

  static String log = "";

  /**
   * Main program
   *
   * @param args args
   */
  public static void main(String[] args) {

    for (int i = 0; i < args.length; ++i) {
      if (args[i].equals("test")) {
        TEST = true;
        wiki = new Wiki("test.wikipedia.org");
      }
    }

    System.out.println("\n*************************************************************************");
    System.out.println(" Category mover for Wikimedia Commons ");
    System.out.println("*************************************************************************");

    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
    Logger.getLogger("wiki").setLevel(Level.OFF);

    for (;;) {
      try {
        System.out.print("\n > User: ");
        user = bufferRead.readLine();
        System.out.print(" > Password: ");
        String password = bufferRead.readLine();

        System.out.print("\n[.] Logging in...");
        wiki.login(user, password);
        System.out.print(" OK!\n");
        break;
      } catch (IOException ex) {
        System.out.println("[!] Error!");
      } catch (FailedLoginException ex) {
        System.out.println("\n[!] Could not login. Wrong password?");
      }
    }

    System.out.println("\n Enter file name");
    System.out.println(" Enter 0 for exit.");

    for (;;) {
      System.out.print("\n > ");

      try {
        String file = bufferRead.readLine();

        if (file.equals("0")) {
          // exit 
          System.exit(0);

        } else {
          // load file
          try (BufferedReader br = new BufferedReader(new FileReader(file + ".txt"))) {
            for (String line; (line = br.readLine()) != null;) {
              String[] categories = line.split(" \\| ");
              String source = categories[0];
              String destination = categories[1];

              if (categories.length != 2) {
                System.out.println("[!] " + categories.length);
                continue;
              }

              System.out.println("[.] Moving from '" + source + "' to '" + destination + "'");
              String[] members = wiki.getCategoryMembers(source);
              if (members.length == 0) {
                System.out.println("[!] Source category looks empty");
                continue;
              }
              System.out.println("[!] Moving " + members.length + " category members");
              System.out.print("[.] ");
              for (String member : members) {
                String text = wiki.getPageText(member);
                text = text.replace(source, destination);
                wiki.edit(member, text, "move to category [[Category:" + destination + "|" + destination + "]]");
                System.out.print("|");
              }
              System.out.print(" Done!\n");
            }
          } catch (LoginException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
          }
        }

      } catch (IOException ex) {
        System.out.println("[!] Womething went wrong!");
      }
    }
  }
}
