package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Location;
import com.tallcraft.githubtickets.ticket.Ticket;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IssueConverter {
    // Max github issue title length
    private static final int maxTitleLength = 50;
    private static IssueConverter ourInstance = new IssueConverter();

    private IssueConverter() {
    }

    public static IssueConverter getInstance() {
        return ourInstance;
    }

    /**
     * Get value from issue body
     *
     * @param issue Issue to extract value from
     * @param key   Key of key-value pair
     * @return Extracted value
     */
    private static String getValue(Issue issue, String key) {
        // TODO: handle exception
        Pattern pattern = Pattern.compile(key + ": (.*)");
        Matcher matcher = pattern.matcher(issue.getBody());

        if (matcher.find()) {
            return matcher.group(0);
        }
        return null; // Not found
    }

    /**
     * Convert Ticket object to Issue Object
     *
     * @param ticket Ticket object to convert
     * @return Issue object from ticket data
     */
    Issue ticketToIssue(Ticket ticket) {
        Issue issue = new Issue();
        Label serverLabel = new Label();

        issue.setLabels(new ArrayList<>(Collections.singletonList(serverLabel)));
        issue.setTitle(getIssueTitle(ticket));
        issue.setBody(getIssueBody(ticket));

        serverLabel.setName("Server: " + ticket.getServerName());

        return issue;
    }

    /**
     * Convert Issue object to Ticket Object
     *
     * @param issue Issue Object to convert
     * @return Ticket object from issue data
     */
    Ticket issueToTicket(Issue issue) {
        Ticket ticket = new Ticket();

        ticket.setTimestamp(issue.getCreatedAt());

        String uuid = getValue(issue, "UUID");

        if (uuid != null) {
            try {
                ticket.setPlayerUUID(UUID.fromString(uuid));
            } catch (IllegalArgumentException ex) {
                // TODO display console error message? throw up in stack until reached mc component?
            }
        }

        ticket.setPlayerName(getValue(issue, "Player"));
        ticket.setServerName(getValue(issue, "Server"));
        ticket.setWorldName(getValue(issue, "World"));
        ticket.setLocation(Location.fromString(getValue(issue, "Location")));

        return ticket;
    }

    /**
     * Generate issue title from ticket object
     *
     * @param ticket Object which holds ticket info
     * @return Issue title suitable for github
     */
    private String getIssueTitle(Ticket ticket) {
        String result = ticket.getPlayerName() + ": " + ticket.getBody();
        // Limit length if needed
        if (result.length() > maxTitleLength) {
            result = result.substring(0, maxTitleLength - 3) + " (...)";
        }
        return result;
    }

    /**
     * Generate issue body from ticket object
     *
     * @param ticket Object which holds ticket info
     * @return Issue body suitable for github
     */
    private String getIssueBody(Ticket ticket) {
        return "Player: " + ticket.getPlayerName() + "\n"
                + "UUID: " + ticket.getPlayerUUID() + "\n"
                + "Server: " + ticket.getServerName() + "\n"
                + "World: " + ticket.getWorldName() + "\n"
                + "Location: " + ticket.getLocation() + "\n\n"
                + ticket.getBody();
    }
}
