package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Location;
import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketComment;
import org.eclipse.egit.github.core.Comment;
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
    private static Pattern bodyPattern = Pattern.compile("(\\r\\n\\r\\n|\\n\\n)(.*)", Pattern.DOTALL);

    private IssueConverter() {
    }

    static IssueConverter getInstance() {
        return ourInstance;
    }

    /**
     * Get value from issue body
     *
     * @param body Body to extract value from
     * @param key   Key of key-value pair
     * @return Extracted value
     */
    private static String getValue(String body, String key) {
        Pattern pattern = Pattern.compile(key + ": (.*)");
        Matcher matcher = pattern.matcher(body);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // Not found
    }

    /**
     * Convert Issue object to Ticket Object
     *
     * @param fullIssue Issue Object with comments to convert
     * @return Ticket object from issue data
     */
    Ticket issueToTicket(FullIssue fullIssue) {
        Ticket ticket = new Ticket();

        Issue issue = fullIssue.getIssue();
        List<Comment> comments = fullIssue.getComments();

        try {
            ticket.setId(issue.getNumber());
            ticket.setOpen(issue.getState().equals(IssueService.STATE_OPEN));
            ticket.setTimestamp(issue.getCreatedAt());

            String uuid = getValue(issue.getBody(), "UUID");

            if (uuid != null) {
                try {
                    ticket.setPlayerUUID(UUID.fromString(uuid));
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }

            ticket.setPlayerName(getValue(issue.getBody(), "Player"));
            ticket.setServerName(getValue(issue.getBody(), "Server"));
            ticket.setWorldName(getValue(issue.getBody(), "World"));
            ticket.setLocation(Location.fromString(getValue(issue.getBody(), "Location")));
            ticket.setBody(parseBody(issue.getBody()));

            if (comments != null) {
                // Convert comments
                ticket.setComments(comments
                        .stream()
                        .map(this::commentToTicketComment)
                        .collect(Collectors.toCollection(LinkedList::new)));
            }

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            // Error while parsing ticket
            return null;
        }
        return ticket;
    }

    /**
     * Convert Ticket object to Issue Object
     *
     * @param ticket Ticket object to convert
     * @return Issue object from ticket data
     */
    FullIssue ticketToIssue(Ticket ticket) {
        Issue issue = new Issue();
        Label serverLabel = new Label();

        serverLabel.setName("Server: " + ticket.getServerName());

        issue.setLabels(new ArrayList<>(Collections.singletonList(serverLabel)));
        issue.setTitle(printIssueTitle(ticket));
        issue.setBody(printIssueBody(ticket));

        // Get ticket comments
        LinkedList<TicketComment> comments = ticket.getComments();

        // Convert comments to strings
        LinkedList<String> convertedComments = comments
                .stream()
                .map(this::printCommentBody)
                .collect(Collectors.toCollection(LinkedList::new));

        return FullIssue.fromString(issue, convertedComments);
    }

    /**
     * Convert collection of issues to tickets
     *
     * @param issues Issue objects to convert
     * @return Collection of tickets
     */
    List<Ticket> issueToTicket(Collection<FullIssue> issues) {
        List<Ticket> tickets = new LinkedList<>();
        for (FullIssue issue : issues) {
            Ticket ticket = issueToTicket(issue);
            if (ticket != null) tickets.add(ticket);
        }
        return tickets;
    }


    /**
     * Converts github comment to ticket comment
     *
     * @param comment comment representation from github with metadata in body
     * @return internal comment format with seperated fields
     */
    TicketComment commentToTicketComment(Comment comment) {
        String uuidStr = getValue(comment.getBody(), "UUID");
        UUID uuid = null;
        String body = null;

        if (uuidStr != null) {
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }

        //  Get display name, fallback to github name
        String displayName = getValue(comment.getBody(), "Name");
        if (displayName == null) {
            // If display name is not present we assume that body comes from github web, use it all
            // TODO: Find a better way to detect this!
            body = comment.getBody();

            // Can be ticket bot name
            displayName = comment.getUser().getName();

            // If no name can be found use static
            if (displayName == null) {
                displayName = "Staff";
            }
        }

        // If body wasn't already set previously parse it from comment
        if (body == null) {
            body = parseBody(comment.getBody());
        }


        return new TicketComment(comment.getCreatedAt(), uuid, displayName, body);
    }


    /**
     * Parse body from printed string
     *
     * @param body containing metadata + body
     * @return body string only
     */
    private String parseBody(String body) {
        Matcher matcher = bodyPattern.matcher(body);

        while (matcher.find()) {
            String match = matcher.group(2);
            if (match != null) return match;
        }
        return null;
    }

    /**
     * Print a comment object to string to be pushed to github
     *
     * @param comment comment to be converted
     * @return api ready string representation of comment
     */
    String printCommentBody(TicketComment comment) {
        return "Name: " + comment.getDisplayName() + "\n"
                + "UUID: " + comment.getPlayerUUID() + "\n" + "\n"
                + comment.getBody();
    }


    /**
     * Generate issue title from ticket object
     *
     * @param ticket Object which holds ticket info
     * @return Issue title suitable for github
     */
    private String printIssueTitle(Ticket ticket) {
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
    private String printIssueBody(Ticket ticket) {
        return "Player: " + ticket.getPlayerName() + "\n"
                + "UUID: " + ticket.getPlayerUUID() + "\n"
                + "Server: " + ticket.getServerName() + "\n"
                + "World: " + ticket.getWorldName() + "\n"
                + "Location: " + ticket.getLocation() + "\n\n"
                + ticket.getBody();
    }
}
