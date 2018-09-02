package uk.gleissner.jutil.spring.batch.rest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gleissner.jutil.spring.batch.rest.domain.Job;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping(value = "/job", produces = "application/hal+json")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public Collection<Job> all() {
        return jobService.jobs();
    }

    @PutMapping("/{jobName}")
    public JobExecution put(@PathVariable String jobName) {
        return jobService.launch(jobName);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, String> errorResponse(Exception ex, HttpServletResponse response) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("errorMessage", ex.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();
        errorMap.put("errorStackTrace", stackTrace);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return errorMap;
    }
}
