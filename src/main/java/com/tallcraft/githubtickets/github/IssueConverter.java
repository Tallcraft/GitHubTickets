package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Location;
import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketComment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.IssueService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static String getValue(Issue issue, String key) {
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
    private static String getTicketBody(Issue issue) {
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
    Map<Issue, List<String>> ticketToIssue(Ticket ticket) {
        Issue issue = new Issue();
        Label serverLabel = new Label();

        serverLabel.setName("Server: " + ticket.getServerName());

        issue.setLabels(new ArrayList<>(Collections.singletonList(serverLabel)));
        issue.setTitle(getIssueTitle(ticket));
        issue.setBody(getIssueBody(ticket));

        // Get ticket comments
        LinkedList<TicketComment> comments = ticket.getComments();

        // Convert comments to strings
        LinkedList<String> convertedComments = comments.stream().map(this::getCommentBody)
                .collect(Collectors.toCollection(LinkedList::new));

        return Map.of(issue, convertedComments);
    }

    /**
     * Convert Issue object to Ticket Object
     *
     * @param issue Issue Object to convert
     * @return Ticket object from issue data
     */
    Ticket issueToTicket(Issue issue) {
        Ticket ticket = new Ticket();

        try {
            ticket.setId(issue.getNumber());
            ticket.setOpen(issue.getState().equals(IssueService.STATE_OPEN));
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

            // TODO: comments
        } catch (IllegalArgumentException ex) {
            // Error while parsing ticket
            return null;
        }
        return ticket;
    }

    private String getCommentBody(TicketComment comment) {
        return "Name: " + comment.getDisplayName() + "\n"
                + "UUID: " + comment.getPlayerUUID() + "\n" + "\n"
                + comment.getBody();
    }

    /**
     * Convert collection of issues to tickets
     *
     * @param issues Issue objects to convert
     * @return Collection of tickets
     */
    List<Ticket> issueToTicket(Collection<Issue> issues) {
        List<Ticket> tickets = new LinkedList<>();
        for (Issue issue : issues) {
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
