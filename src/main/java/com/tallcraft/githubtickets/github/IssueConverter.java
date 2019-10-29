package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Location;
import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketComment;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
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
     * @param body Body to extract value from
     * @param key  Key of key-value pair
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
     * Extract ticket body text from issue
     *
     * @param body Issue body to extract body text from
     * @return body text of issue without key value pairs
     */
    private static String getTicketBody(String body) {
        Matcher matcher = ticketBodyPattern.matcher(body);
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
    GHIssue ticketToIssue(GHRepository repository, Ticket ticket) throws IOException {
        GHIssue issue = repository.createIssue(getIssueTitle(ticket))
                .body(getIssueBody(ticket))
                .label("Server: " + ticket.getServerName()).create();
        if (issue == null) {
            return null;
        }

        ticket.getComments().forEach(ticketComment -> {
            try {
                issue.comment(ticketCommentToIssueCommentStr(ticketComment));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return issue;
    }

    /**
     * Converts github comment to ticket comment
     *
     * @param issueComment comment representation from github with metadata in body
     * @return internal comment format with seperated fields
     */
    private TicketComment issueCommentToTicketComment(GHIssueComment issueComment) {
        String uuidStr = getValue(issueComment.getBody(), "UUID");
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
        String displayName = getValue(issueComment.getBody(), "Name");
        if (displayName == null) {
            // If display name is not present we assume that body comes from github web, use it all
            body = issueComment.getBody();

            // Can be ticket bot name
            try {
                displayName = issueComment.getUser().getName();
            } catch (IOException e) {
                // failed to get display name, leave it null
            }

            // If no name can be found use static
            if (displayName == null) {
                displayName = "Staff";
            }
        }

        // If body wasn't already set previously parse it from comment
        if (body == null) {
            body = getTicketBody(issueComment.getBody());
        }

        Date createdAt = null;
        try {
            createdAt = issueComment.getCreatedAt();
        } catch (IOException e) {
            // Could not get / parse date
        }

        return new TicketComment(createdAt, uuid, displayName, body);
    }


    /**
     * Convert Issue object to Ticket Object
     *
     * @param issue Issue Object to convert
     * @return Ticket object from issue data
     */
    Ticket issueToTicket(GHIssue issue, boolean includeComments) {
        if (issue == null) {
            return null;
        }
        Ticket ticket = new Ticket();
        String issueBody = issue.getBody();

        try {
            ticket.setId(issue.getNumber());
            ticket.setOpen(issue.getState().equals(GHIssueState.OPEN));
            ticket.setTimestamp(issue.getCreatedAt());

            String uuid = getValue(issueBody, "UUID");

            if (uuid != null) {
                try {
                    ticket.setPlayerUUID(UUID.fromString(uuid));
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }

            ticket.setPlayerName(getValue(issueBody, "Player"));
            ticket.setServerName(getValue(issueBody, "Server"));
            ticket.setWorldName(getValue(issueBody, "World"));
            ticket.setLocation(Location.fromString(getValue(issueBody, "Location")));
            ticket.setBody(getTicketBody(issueBody));
        } catch (IllegalArgumentException | IOException ex) {
            // Error while parsing ticket
            return null;
        }

        if (includeComments) {
            // Issue comments => Ticket comments
            try {
                // TODO: issue.getComments() needs an api call, it should probably be moved somewhere else
                List<GHIssueComment> comments = issue.getComments();
                if (comments != null) {
                    // Convert comments
                    ticket.setComments(comments
                            .stream()
                            .map(this::issueCommentToTicketComment)
                            .collect(Collectors.toCollection(LinkedList::new)));
                }
            } catch (IOException ex) {
                // Error while parsing ticket comments
            }
        }

        return ticket;
    }

    /**
     * Convert collection of issues to tickets
     *
     * @param issues Issue objects to convert
     * @return Collection of tickets
     */
    List<Ticket> issueToTicket(Collection<GHIssue> issues, boolean includeComments) {
        List<Ticket> tickets = new LinkedList<>();
        for (GHIssue issue : issues) {
            Ticket ticket = issueToTicket(issue, includeComments);
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

    String ticketCommentToIssueCommentStr(TicketComment comment) {
        return "Name: " + comment.getDisplayName() + "\n"
                + "UUID: " + comment.getPlayerUUID() + "\n" + "\n"
                + comment.getBody();
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
