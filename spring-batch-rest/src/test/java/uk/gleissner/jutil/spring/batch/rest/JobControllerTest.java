package uk.gleissner.jutil.spring.batch.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gleissner.jutil.spring.batch.rest.domain.JobMother.jobExecution;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Test
    public void jobs() throws Exception {
        when(jobService.getJobExecutions(any(), any(), any(), any()))
                .thenReturn(newArrayList(jobExecution("1"), jobExecution("2")));

        mockMvc.perform(get("/jobExecutions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"jobId\":\"1\",\"startTime\":null,\"endTime\":null," +
                        "\"status\":null},{\"jobId\":\"2\",\"startTime\":null,\"endTime\":null,\"status\":null}]"));
    }
}
