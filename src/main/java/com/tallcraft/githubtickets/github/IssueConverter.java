package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Location;
import com.tallcraft.githubtickets.ticket.Ticket;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts between Issues and Tickets by generating Strings and parsing from Strings
 */
class IssueConverter {
    // Max github issue title length
    private static final int maxTitleLength = 50;
    private static IssueConverter ourInstance = new IssueConverter();
    private static Pattern ticketBodyPattern = Pattern.compile("\\n\\n(.*)", Pattern.DOTALL);

    private IssueConverter() {
    }

    static IssueConverter getInstance() {
        return ourInstance;
    }

    /**
     * Get value from issue body
     *
     * @param issue Issue to extract value from
     * @param key   Key of key-value pair
     * @return Extracted value
     */
    private static String getValue(GHIssue issue, String key) {
        Pattern pattern = Pattern.compile(key + ": (.*)");
        Matcher matcher = pattern.matcher(issue.getBody());

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // Not found
    }

    /**
     * Extract ticket body text from issue
     *
     * @param issue Issue to extract body text from
     * @return body text of issue without key value pairs
     */
    private static String getTicketBody(GHIssue issue) {
        Matcher matcher = ticketBodyPattern.matcher(issue.getBody());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Convert Ticket object to Issue Object
     *
     * @param ticket Ticket object to convert
     * @return Issue object from ticket data
     */
    GHIssueBuilder ticketToIssue(GHRepository repository, Ticket ticket) {
        return repository.createIssue(getIssueTitle(ticket))
                .body(getIssueBody(ticket))
                .label("Server: " + ticket.getServerName());
    }

    /**
     * Convert Issue object to Ticket Object
     *
     * @param issue Issue Object to convert
     * @return Ticket object from issue data
     */
    Ticket issueToTicket(GHIssue issue) {
        if (issue == null) {
            return null;
        }

        Ticket ticket = new Ticket();

        try {
            ticket.setId(issue.getNumber());
            ticket.setOpen(issue.getState().equals(GHIssueState.OPEN));
            ticket.setTimestamp(issue.getCreatedAt());

            String uuid = getValue(issue, "UUID");

            if (uuid != null) {
                try {
                    ticket.setPlayerUUID(UUID.fromString(uuid));
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }

            ticket.setPlayerName(getValue(issue, "Player"));
            ticket.setServerName(getValue(issue, "Server"));
            ticket.setWorldName(getValue(issue, "World"));
            ticket.setLocation(Location.fromString(getValue(issue, "Location")));
            ticket.setBody(getTicketBody(issue));
        } catch (IllegalArgumentException | IOException ex) {
            // Error while parsing ticket
            return null;
        }
        return ticket;
    }

    /**
     * Convert collection of issues to tickets
     *
     * @param issues Issue objects to convert
     * @return Collection of tickets
     */
    List<Ticket> issueToTicket(Collection<GHIssue> issues) {
        List<Ticket> tickets = new LinkedList<>();
        for (GHIssue issue : issues) {
            Ticket ticket = issueToTicket(issue);
            if (ticket != null) tickets.add(ticket);
        }
        return tickets;
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
