package com.tallcraft.githubtickets.github;

import com.tallcraft.githubtickets.ticket.Ticket;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;

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

    public long createIssue(Ticket ticket) throws IOException {
        if (!isConnected) {
            throw new RuntimeException("Not connected to GitHub");
        }

        // Convert to issue object
        Issue issue = issueConverter.ticketToIssue(ticket);

        // API call to create issue
        Issue createdIssue = issueService.createIssue(repository, issue);
        return createdIssue.getId();
    }


}
