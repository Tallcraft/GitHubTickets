package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.GithubTickets;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Interfaces between this application and GitHub API
 */
public class GitHubController {
    private static GitHubController ourInstance = new GitHubController();
    private static IssueConverter issueConverter = IssueConverter.getInstance();
    private GitHubClient client;
    private RepositoryService repService;
    private IssueService issueService;
    private Repository repository;
    // Boolean to store current connection state to api
    private boolean isConnected = false;
    private LinkedBlockingQueue<Runnable> apiTasks = new LinkedBlockingQueue<>();
    private final ApiWorker apiWorker = new ApiWorker(apiTasks);

    public static GitHubController getInstance() {
        return ourInstance;
    }

    /**
     * Connect to GitHub API
     *
     * @param user           GitHub username
     * @param password       GitHub password
     * @param repositoryUser Issue Repository Owner
     * @param repositoryName Issue Repository Name
     * @throws IOException If an error occurs while establishing api connection
     */
    public void connect(String user, String password, String repositoryUser, String repositoryName, GithubTickets plugin) throws IOException {
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

        // Set api connection status flag
        isConnected = true;

        // Start async worker for api requests
        apiWorker.runTaskAsynchronously(plugin);
    }

    /**
     * Create Issue on Github from Ticket
     * API Call is async
     *
     * @param ticket Ticket data to create issue with
     * @return Future which resolves with ticket id or exception
     */
    public Future<Integer> createTicket(Ticket ticket) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        // Create async task
        Runnable task = () -> {
            try {
                future.complete(createTicketSync(ticket));
            } catch (IOException e) {
                // Forward exception to future
                future.completeExceptionally(e);
            }
        };
        // Add api call to tasks list
        apiTasks.add(task);
        return future;
    }

    /**
     * Get GitHub issue by id
     *
     * @param id ID to query for
     * @return Future resolving with Ticket or exception
     */
    public Future<Ticket> getTicket(int id) {
        CompletableFuture<Ticket> future = new CompletableFuture<>();
        // Create async task
        Runnable task = () -> {
            try {
                future.complete(getTicketSync(id));
            } catch (IOException e) {
                // Forward exception to future
                future.completeExceptionally(e);
            }
        };
        // Add api call to tasks list
        apiTasks.add(task);
        return future;
    }


    /**
     * Get a list of open tickets
     *
     * @return Future which resolves with ticket list or exception
     */
    public Future<List<Ticket>> getOpenTickets() {
        CompletableFuture<List<Ticket>> future = new CompletableFuture<>();
        // Create async task
        Runnable task = () -> {
            try {
                future.complete(getOpenTicketsSync());
            } catch (IOException e) {
                // Forward exception to future
                future.completeExceptionally(e);
            }
        };
        // Add api call to tasks list
        apiTasks.add(task);
        return future;
    }

    /**
     * Change status of ticket to either closed or open
     *
     * @param id   Ticket ID
     * @param open true = open, false = closed
     * @return true on success, false on ticket not found
     */
    public Future<Ticket> changeTicketStatus(int id, boolean open) {
        CompletableFuture<Ticket> future = new CompletableFuture<>();
        // Create async task
        Runnable task = () -> {
            try {
                future.complete(changeTicketStatusSync(id, open));
            } catch (IOException e) {
                // Forward exception to future
                future.completeExceptionally(e);
            }
        };
        // Add api call to tasks list
        apiTasks.add(task);
        return future;
    }


    /**
     * Change status of ticket to either closed or open
     *
     * @param id   Ticket ID
     * @param open true = open, false = closed
     * @return Ticket object which status was changed or null if ticket not found by id
     * @throws IOException API error
     */
    private Ticket changeTicketStatusSync(int id, boolean open) throws IOException {
        Issue issue;
        try {
            issue = issueService.getIssue(repository, id);
        } catch (RequestException ex) {
            // Don't throw not found exceptions, but return null issue
            if (ex.getStatus() == 404) {
                return null;
            }
            throw ex;
        }

        // Change status of github issue and get updated issue object
        issue = changeTicketStatus(issue, open);
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
    private int createTicketSync(Ticket ticket) throws IOException {
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
     * Get Ticket by id
     *
     * @param id ID to query for
     * @return Ticket representation of GitHub issue with matching ID
     * @throws IOException If an error occurs during api communication
     */
    private Ticket getTicketSync(int id) throws IOException {
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


    /**
     * Change status of ticket to either closed or open
     *
     * @param issue GitHub API Issue object
     * @param open  true = open, false = closed
     * @throws IOException API error
     */
    private Issue changeTicketStatus(Issue issue, boolean open) throws IOException {
        issue.setState(open ? IssueService.STATE_OPEN : IssueService.STATE_CLOSED);
        return issueService.editIssue(repository, issue);
    }


    /**
     * Get a list of open tickets
     *
     * @return list of tickets with state open
     * @throws IOException API error
     */
    private List<Ticket> getOpenTicketsSync() throws IOException {
        Map<String, String> issueFilters = Map.of(IssueService.FILTER_STATE, IssueService.STATE_OPEN);
        List<Issue> issues = issueService.getIssues(repository, issueFilters);
        return issueConverter.issueToTicket(issues);
    }
}
