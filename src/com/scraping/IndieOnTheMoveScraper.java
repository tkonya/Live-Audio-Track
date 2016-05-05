package com.scraping;

import com.models.Venue;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Trevor on 5/5/2016.
 */
public class IndieOnTheMoveScraper extends VenueScraper {

    public static void main(String[] args) {
        Venue venue = scrapeVenuePage("https://www.indieonthemove.com/venues/view/miami-live-miami-florida");
        System.out.println(venue.toString());
//        scrape();
    }

    public static List<Venue> scrape() {

        List<Venue> venues = new ArrayList<>();

        try {

            // gather the links to every venue
            Set<String> venueURLs = new HashSet<>();
            Document document = Jsoup.connect("https://www.indieonthemove.com/venues").get();

            boolean morePages = true;
            while (morePages) {
                Elements venueLinks = document.select("td.venue-name a.title");
                System.out.println("Found " + venueLinks.size() + " venue links on this page to scrape");
                for (Element venueLink : venueLinks) {
                    venueURLs.add("https://www.indieonthemove.com/venues" + venueLink.attr("href"));
                }

                Elements nextButtons = document.select("li.next a[href]");
                if (!nextButtons.isEmpty()) {
                    sleepBetween(1, 4);
                    document = Jsoup.connect(nextButtons.first().attr("href")).get();
                } else {
                    morePages = false;
                }

            }

            System.out.println("Got " + venueURLs.size() + " venue URLs to scrape");


            // scrape each individual page now
            for (String venueURL : venueURLs) {
                sleepBetween(1, 4);
                scrapeVenuePage(venueURL);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return venues;
    }

    public static Venue scrapeVenuePage(String venueURL) {
        Venue venue = new Venue();
        try {

            Document document = Jsoup.connect(venueURL).get();

            venue.setSource(venueURL);

            // name
            Elements elements = document.select("div.venue-details div.alignleft h1");
            if (!elements.isEmpty()) {
                String name = elements.first().text().trim();
                if (name.endsWith("Edit")) {
                    name = name.substring(0, name.length() - 5).trim();
                }
                venue.setName(name);
            }

            // address
            elements = document.select("div.venue-details div.alignleft ul li p.nopad");
            if (!elements.isEmpty()) {
                String inner = elements.html();
                String [] addresses = inner.split("<br>");
                if (addresses.length == 2) {
                    venue.setStreet1(addresses[0]);
                    String [] addresses2 = addresses[1].split(" ");
                    if (addresses2.length == 3) {
                        venue.setCity(addresses2[0].replace(",", ""));
                        venue.setState(addresses2[1]);
                        venue.setZip(addresses2[2]);
                    }
                }
            }

            // phone
            elements = document.select("div.venue-details div.alignleft ul li");
            if (!elements.isEmpty()) {
                for (Element element : elements) {
                    String phoneNumberMaybe = element.text();
                    if (phoneNumberMaybe.replaceAll("\\D+", "").length() > 6 && (StringUtils.countMatches(phoneNumberMaybe, ".") > 1 || StringUtils.countMatches(phoneNumberMaybe, "-") > 1)) {
                        venue.setPhone(element.text().trim());
                    }
                }
            }

            // website
            elements = document.select("div.venue-details div.alignleft ul li a[href]");
            if (!elements.isEmpty()) {
                venue.setWebsite(elements.first().attr("href"));
            }

            // genres
            elements = document.select("div.venue-details div.alignleft ul li:contains(Genres)");
            if (!elements.isEmpty()) {
                venue.setGenre(elements.first().text().replace("Genres:", "").trim());
            }

            // capacity
            elements = document.select("div.venue-details div.alignleft ul li:contains(Capacity)");
            if (!elements.isEmpty()) {
                String capacityString = elements.first().text().replace("Capacity:", "").trim();
                try {
                    int capacity = Integer.parseInt(capacityString);
                    venue.setCapacity(capacity);
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse capacity: " + capacityString + " on page " + venueURL);
                }
            }

            // age
            elements = document.select("div.venue-details div.alignleft ul li:contains(Age)");
            if (!elements.isEmpty()) {
                String ageString = elements.first().text().replace("Age:", "").trim();
                if (ageString.equals("All")) {
                    venue.setMinAge(0);
                } else {
                    try {
                        int minAge = Integer.parseInt(ageString.replace("+", ""));
                        venue.setMinAge(minAge);
                    } catch (NumberFormatException e) {
                        System.out.println("Failed to parse age: " + ageString + " on page " + venueURL);
                    }
                }
            }

            // notes
            elements = document.select("div.venSections div.objcontent");
            if (elements.size() > 1) {
                String notes = elements.get(1).text();

                elements = elements.get(1).select("a[href]");
                if (!elements.isEmpty()) {
                    for (Element element : elements) {
                        notes += " " + element.attr("href");
                    }
                }

                if (!notes.trim().isEmpty()) {
                    venue.setNotes(notes);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return venue;
    }
}
