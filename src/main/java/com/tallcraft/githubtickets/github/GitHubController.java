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

    public static GitHubController getInstance() {
        return ourInstance;
    }


    private GitHubClient client;
    private RepositoryService repService;
    private IssueService issueService;
    private Repository repository;


    public void connect(String user, String password, String repositoryUser, String repositoryName) throws IOException {
        // Initialize GitHubController client
        this.client = new GitHubClient();
        this.client.setCredentials(user, password);

        // Get repository service
        this.repService = new RepositoryService(this.client);

        // Get issue service
        this.issueService = new IssueService(this.client);

        // Search for ticket repository
        // TODO: support other repos, not only owned
        this.repository = this.repService.getRepository(repositoryUser, repositoryName);
    }

    public long createIssue(Ticket ticket) throws IOException {
        Issue issue = new Issue();
        issue.setTitle(ticket.getIssueTitle()).setBody(ticket.getIssueBody());
        Issue createdIssue = this.issueService.createIssue(this.repository, issue);
        return createdIssue.getId();
    }

    public void listIssues() throws IOException {
        for(Issue issue : this.issueService.getIssues()) {
            System.out.println(issue.getTitle());
        }
    }

}
