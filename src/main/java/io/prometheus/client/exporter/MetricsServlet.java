package io.prometheus.client.exporter;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.common.TextFormat;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.security.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The MetricsServlet class exists to provide a simple way of exposing the metrics values.
 *
 */
public class MetricsServlet extends HttpServlet {

  private CollectorRegistry registry;
  private static Counter requests = Counter.build()
          .name("requests_total").help("Total requests.").register();
  private static Counter failedRequests = Counter.build()
          .name("requests_failed_total").help("Total failed requests.").register();
  private static Gauge inprogressRequests = Gauge.build()
          .name("inprogress_requests").help("Inprogress requests.")
          .register();
  private static Gauge timeToService = Gauge.build()
          .name("service_time").help("Time to serve")
          .register();

  /**
   * Construct a MetricsServlet for the default registry.
   */
  public MetricsServlet() {
    this(CollectorRegistry.defaultRegistry);
  }




  /**
   * Construct a MetricsServlet for the given registry.
   * @param registry collector registry
   */
  public MetricsServlet(CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
          throws ServletException, IOException {

    long timestampStart = System.currentTimeMillis();
    requests.inc();
    inprogressRequests.inc();
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType(TextFormat.CONTENT_TYPE_004);

    Writer writer = resp.getWriter();
    try {
      TextFormat.write004(writer, registry.filteredMetricFamilySamples(parse(req)));
      writer.flush();
      try {
        Thread.currentThread().sleep((long) (Math.abs(Math.random()) * 1000));

      } catch (InterruptedException ie) {

      }
    } finally {
      if (Math.random() < 0.1d) {
        failedRequests.inc();
      }
      inprogressRequests.dec();
      timeToService.set(System.currentTimeMillis() - timestampStart);
      writer.close();
    }
  }

  private Set<String> parse(HttpServletRequest req) {
    String[] includedParam = req.getParameterValues("name[]");
    if (includedParam == null) {
      return Collections.emptySet();
    } else {
      return new HashSet<String>(Arrays.asList(includedParam));
    }
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
          throws ServletException, IOException {
    doGet(req, resp);
  }

}
