package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Ticket;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;


/**
 * Interfaces between this application and GitHub API
 */
public class GitHubController {
    private static GitHubController ourInstance = new GitHubController();
    private static IssueConverter issueConverter = IssueConverter.getInstance();


    // Either user/password or OAuth is needed for GitHub auth
    private String user;
    private String password;
    private String oauth;

    private GHRepository repository;

    // Boolean to store current api connection state
    private boolean isConnected = false;

    public static GitHubController getInstance() {
        return ourInstance;
    }

    /**
     * Connect to GitHub API
     *
     * @param repositoryUser Issue Repository Owner
     * @param repositoryName Issue Repository Name
     * @throws IOException If an error occurs while establishing api connection
     */
    public void connect(String repositoryUser, String repositoryName) throws IOException {
        assert (!isConnected);

        if (repositoryUser == null || repositoryUser.isEmpty()) {
            throw new IllegalArgumentException("'repositoryUser' must not be empty");
        }
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("'repositoryName' must not be empty");
        }

        // Initialize GitHubController client
        GitHub client;
        if (oauth != null && !oauth.isEmpty()) {
            client = GitHub.connectUsingOAuth(oauth);
        } else if (user != null && password != null && !user.isEmpty() && !password.isEmpty()) {
            client = GitHub.connectUsingPassword(user, password);
        } else {
            throw new IllegalArgumentException("No credentials set");
        }

        repository = client.getRepository(repositoryUser + "/" + repositoryName);

        // Set api connection status flag
        isConnected = true;
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Set OAuth token to authenticate with GitHub
     *
     * @param oauth token
     */
    public void setOauth(String oauth) {
        assert (!isConnected && user == null && password == null);
        this.oauth = oauth;
    }

    /**
     * Set credentials used to authenticate with GitHub
     *
     * @param user     GitHub username
     * @param password GitHub password
     */
    public void setCredentials(String user, String password) {
        assert (!isConnected && oauth == null);
        this.user = user;
        this.password = password;
    }


    /**
     * Change status of ticket to either closed or open
     *
     * @param id   Ticket ID
     * @param open true = open, false = closed
     * @return Ticket object which status was changed or null if ticket not found by id
     * @throws IOException API error
     */
    public Ticket changeTicketStatus(int id, boolean open) throws IOException {
        GHIssue issue;

        issue = repository.getIssue(id);
        if (issue == null) {
            return null;
        }

        // Change status of github issue and get updated issue object
        changeTicketStatus(issue, open);
        // Convert issue object to ticket and return
        return issueConverter.issueToTicket(issue);
    }


    /**
     * Create Issue on Github from Ticket
     *
     * @param ticket Ticket data to create issue from
     * @return Issue / Ticket ID
     * @throws IOException If an error occurs during api communication
     */
    public int createTicket(Ticket ticket) throws IOException {
        if (!isConnected) {
            throw new RuntimeException("Not connected to GitHub");
        }

        // Convert to issue object
        GHIssueBuilder issueBuilder = issueConverter.ticketToIssue(repository, ticket);
        return issueBuilder.create().getNumber();
    }


    /**
     * Get Ticket by id
     *
     * @param id ID to query for
     * @return Ticket representation of GitHub issue with matching ID
     * @throws IOException If an error occurs during api communication
     */
    public Ticket getTicket(int id) throws IOException {
        return issueConverter.issueToTicket(repository.getIssue(id));
    }


    /**
     * Change status of ticket to either closed or open
     *
     * @param issue GitHub API Issue object
     * @param open  true = open, false = closed
     * @throws IOException API error
     */
    private void changeTicketStatus(GHIssue issue, boolean open) throws IOException {
        if (open) {
            issue.reopen();
        } else {
            issue.close();
        }
    }


    /**
     * Get a list of open tickets
     *
     * @return list of tickets with state open
     * @throws IOException API error
     */
    public List<Ticket> getTickets(boolean filterOpen) throws IOException {
        GHIssueState state = filterOpen ? GHIssueState.OPEN : GHIssueState.ALL;
        return issueConverter.issueToTicket(repository.getIssues(state));
    }
}
