package com.git.ifly6.nsapi.tests;

import com.git.ifly6.nsapi.NSRegion;

import java.io.IOException;

public class UrlTests {

    public static void main(String[] args) throws IOException {

//        NSNation nation = new NSNation("imperium anglorum");
//        nation.populateData();

        NSRegion region = new NSRegion("europe");
        region.populateData();

    }

}
