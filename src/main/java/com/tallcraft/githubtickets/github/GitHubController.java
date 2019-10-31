package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketComment;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.kohsuke.github.*;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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

    private GitHub client;
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

        Cache cache = new Cache(new File("cache"), 10 * 1024 * 1024); // TODO: plugin dir
        OkHttpClient httpClient = (new okhttp3.OkHttpClient.Builder()).cache(cache).build();
        OkHttpConnector connector = new OkHttpConnector(httpClient);
        GitHubBuilder builder = new GitHubBuilder().withConnector(connector);

        // Initialize GitHubController client
        if (oauth != null && !oauth.isEmpty()) {
            builder = builder.withOAuthToken(oauth);
        } else if (user != null && password != null && !user.isEmpty() && !password.isEmpty()) {
            builder = builder.withPassword(user, password);
        } else {
            throw new IllegalArgumentException("No credentials set");
        }

        client = builder.build();

        try {
            repository = client.getRepository(repositoryUser + "/" + repositoryName);
        } catch (GHFileNotFoundException ex) {
            repository = client.createRepository(repositoryName)
                    .private_(true)
                    .issues(true)
                    .description("Ticket repository powered by GitHubTickets. Click on 'Issues' to see a list of tickets")
                    .autoInit(true)
                    .create();
        }

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

    public String getRateLimitInfo() {
        if (!isConnected || client == null) {
            return null;
        }
        GHRateLimit rateLimit;
        try {
            rateLimit = client.getRateLimit();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return rateLimit.toString();
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
        Ticket ticket = issueConverter.issueToTicket(issue, false);
        // Update ticket open status to reflect the change we just applied on GitHub
        ticket.setOpen(open);
        return ticket;
    }


    /**
     * Create Issue on Github from Ticket
     *
     * @param ticket Ticket data to create issue from
     * @return Issue / Ticket ID
     * @throws IOException If an error occurs during api communication
     */
    public Ticket createTicket(Ticket ticket) throws IOException {
        if (!isConnected) {
            throw new RuntimeException("Not connected to GitHub");
        }

        // Convert to issue object and push to repo
        GHIssue issue = issueConverter.ticketToIssue(repository, ticket);
        ticket.setId(issue.getNumber());
        return ticket;
    }

    /**
     * Reply to a ticket
     *
     * @param id      Ticket number
     * @param comment comment to add to ticket
     * @return modified ticket or null if ticket id not found
     * @throws IOException If an error occurs during api communication
     */
    public Ticket addTicketComment(int id, TicketComment comment) throws IOException {
        String commentStr = issueConverter.ticketCommentToIssueCommentStr(comment);
        GHIssue issue;
        try {
            issue = this.repository.getIssue(id);
        } catch (GHFileNotFoundException ex) {
            return null;
        }
        if (issue == null) {
            return null;
        }
        issue.comment(commentStr);

        // While fetching the ticket again from the API would give us more accurate data,
        // we'd have to make another API call, which is costly.
        // Mock the timestamp and return the ticket.
        comment.setTimestamp(new Date());
        return issueConverter.issueToTicket(issue, true);
    }


    /**
     * Get Ticket by id
     *
     * @param id ID to query for
     * @return Ticket representation of GitHub issue with matching ID
     * @throws IOException If an error occurs during api communication
     */
    public Ticket getTicket(int id) throws IOException {
        try {
            return issueConverter.issueToTicket(repository.getIssue(id), true);
        } catch (GHFileNotFoundException ex) {
            return null;
        }

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
     * Get a list of tickets with matching open state
     *
     * @param filterState ticket open state to filter for, true = open, false = closed
     * @return list of tickets with desired state
     * @throws IOException API error
     */
    public List<Ticket> getTickets(boolean filterState, boolean includeComments) throws IOException {
        GHIssueState state = filterState ? GHIssueState.OPEN : GHIssueState.CLOSED;
        return issueConverter.issueToTicket(repository.getIssues(state), includeComments);
    }

    /**
     * Get a list of all tickets
     *
     * @return list of all tickets in repo
     * @throws IOException API error
     */
    public List<Ticket> getTickets(boolean includeComments) throws IOException {
        return issueConverter.issueToTicket(repository.getIssues(GHIssueState.ALL), includeComments);
    }
}
