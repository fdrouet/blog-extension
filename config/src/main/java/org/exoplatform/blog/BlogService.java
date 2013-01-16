package org.exoplatform.blog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.wcm.core.NodeLocation;

/**
 * All the Blog services
 */
@Path("/blog/")
public class BlogService implements ResourceContainer {

  private static final long DEFAULT_NUMBER_OF_POSTS_FETCHED = 5;

  private static final long MAXIMUM_NUMBER_OF_POSTS_FETCHED = 50;

  private static final Log LOG = ExoLogger.getLogger("exo.blog.BlogService");

  /**
   * See {@link org.exoplatform.services.jcr.ext.app.SessionProviderService}.
   */
  private SessionProviderService sessionProviderService;

  public BlogService(RegistryService regService, SessionProviderService sessionProviderService)
      throws Exception {
    this.sessionProviderService = sessionProviderService;
  }


  /**
   * Fetch the maximum of {@link BlogService.DEFAULT_NUMBER_OF_POSTS_FETCHED} number of Posts with a Published state
   * <p/>
   * FIXME : Add pagination to this method to avoid performance and memory problems
   * FIXME : Better handling / wrapping of exceptions
   *
   * @return The list of Published blog posts
   * @throws RepositoryException
   * @throws ParseException      if a date parsing problem occurs
   */
  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPublishedPostsRest() throws RepositoryException, ParseException {
    LOG.info("REST blog list");
    List<BlogPost> posts = getPublishedPosts(DEFAULT_NUMBER_OF_POSTS_FETCHED);

    return Response.ok(posts).build();
  }

  /**
   * Fetch the required number of Posts with a limit of {@link BlogService.MAXIMUM_NUMBER_OF_POSTS_FETCHED}.
   * <p/>
   * FIXME : Add a upper limit to the number of Posts we can fetch at one time (perf)
   *
   * @param number the number of Posts to fetch
   * @return The number of required posts
   * @throws RepositoryException
   * @throws ParseException      if a date parsing problem occurs
   */
  @GET
  @Path("/list/{number}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPublishedPostsRest(@PathParam("number") long number) throws RepositoryException, ParseException {
    LOG.info("REST blog list(" + number + ")");

    List<BlogPost> posts = getPublishedPosts(number);

    return Response.ok(posts).build();
  }

  /**
   * Fetch the required number of Posts
   *
   * @param number the number of Posts to fetch
   * @return the number of required posts
   * @throws RepositoryException
   * @throws ParseException
   */
  public List<BlogPost> getPublishedPosts(long number) throws RepositoryException, ParseException {
    LOG.info("published blog list(" + number + ")");

    NodeLocation nl = new NodeLocation("repository", "collaboration", "/Groups/platform/users/Documents/Blog");
    Node nodeLocation = NodeLocation.getNodeByLocation(nl);
    NodeIterator nodeIterator = nodeLocation.getNodes();

    List<BlogPost> posts = new ArrayList<BlogPost>();

    while (nodeIterator.hasNext() && posts.size() < number) {
      Node node = nodeIterator.nextNode();
      if (node.isNodeType("exo:taxonomyLink")) {
        String blogUUID = node.getProperty("exo:uuid").getString();
        Node blogNode = node.getSession().getNodeByUUID(blogUUID);
        // Check if the node is a Blog Entry and is already published
        if ("exo:blog".equals(blogNode.getPrimaryNodeType().getName()) && blogNode.hasProperty("publication:liveDate")) {
          // FIXME Fetch the published version of the Blog Post (use "publication:liveRevision" property)
          BlogPost blogPost = new BlogPost();
          // exo:title
          blogPost.setTitle(blogNode.getName());
          // exo:owner
          blogPost.setOwner(blogNode.getProperty("exo:owner").getString());
          // publication:liveDate
          blogPost.setPublicationDateByDate(blogNode.getProperty("publication:liveDate").getValue().getDate().getTime());
          // publication:liveRevision

          posts.add(blogPost);
          LOG.info("POST SIZE = " + posts.size());

        }
      }
    }
    return posts;
  }
}
