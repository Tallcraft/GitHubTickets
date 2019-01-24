package com.tallcraft.githubtickets.github;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to hold Github API Issues with their comments used for conversion
 */
public class FullIssue {
    private Issue issue;
    private List<Comment> comments;

    FullIssue(Issue issue, List<Comment> comments) {
        this.issue = issue;
        this.comments = comments;
    }

    static FullIssue fromString(Issue issue, List<String> commentListStr) {
        return new FullIssue(issue, commentListStr.stream()
                .map(comment -> new Comment().setBody(comment))
                .collect(Collectors.toCollection(LinkedList::new)));
    }

    Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}

