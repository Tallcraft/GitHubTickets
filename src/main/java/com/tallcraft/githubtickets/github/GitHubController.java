package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Ticket;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interfaces between this application and GitHub API
 */
public class GitHubController {
    private static GitHubController ourInstance = new GitHubController();
    private static IssueConverter issueConverter = IssueConverter.getInstance();

    public static GitHubController getInstance() {
        return ourInstance;
    }


    private GitHubClient client;
    private RepositoryService repService;
    private IssueService issueService;
    private Repository repository;

    // Boolean to store current connection state to api
    private boolean isConnected = false;

    /**
     * Connect to GitHub API
     *
     * @param user           GitHub username
     * @param password       GitHub password
     * @param repositoryUser Issue Repository Owner
     * @param repositoryName Issue Repository Name
     * @throws IOException If an error occurs while establishing api connection
     */
    public void connect(String user, String password, String repositoryUser, String repositoryName) throws IOException {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("'user' must not be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("'password' must not be empty");
        }
        if (repositoryUser == null || repositoryUser.isEmpty()) {
            throw new IllegalArgumentException("'repositoryUser' must not be empty");
        }
        if (repositoryName == null || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("'repositoryName' must not be empty");
        }

        // Initialize GitHubController client
        client = new GitHubClient();
        client.setCredentials(user, password);

        // Get repository service
        repService = new RepositoryService(client);

        // Get issue service
        issueService = new IssueService(client);

        repository = repService.getRepository(repositoryUser, repositoryName);

        isConnected = true;
    }

    /**
     * Create Issue on Github from Ticket
     * @param ticket Ticket data to create issue from
     * @return Issue / Ticket ID
     * @throws IOException If an error occurs during api communication
     */
    public long createIssue(Ticket ticket) throws IOException {
        if (!isConnected) {
            throw new RuntimeException("Not connected to GitHub");
        }

        // Convert to issue object
        Issue issue = issueConverter.ticketToIssue(ticket);

        // API call to create issue
        Issue createdIssue = issueService.createIssue(repository, issue);
        return createdIssue.getNumber();
    }

    /**
     * Get GitHub issue by id
     * @param id ID to query for
     * @return Ticket representation of GitHub issue with matching ID
     * @throws IOException If an error occurs during api communication
     */
    public Ticket getTicket(int id) throws IOException {
        try {
            Issue issue = issueService.getIssue(repository, id);
            return issueConverter.issueToTicket(issue);
        } catch (RequestException ex) {
            // Don't throw not found exceptions, but return null issue
            if (ex.getStatus() == 404) {
                return null;
            }
            throw ex;
        }
    }

    public List<Ticket> getOpenTickets() throws IOException {
        Map<String, String> issueFilters = Map.of(IssueService.FILTER_STATE, IssueService.STATE_OPEN);
        List<Issue> issues = issueService.getIssues(repository, issueFilters);
        return issueConverter.issueToTicket(issues);
    }
}
