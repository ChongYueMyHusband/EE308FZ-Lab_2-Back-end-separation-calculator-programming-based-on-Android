import com.example.calculate.CalculationResult;
import com.example.calculate.ExpressionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


@RestController
public class Controller {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    public Controller(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("calculate")
    public CalculationResult calculateExpression(@RequestBody ExpressionRequest request) {
        String expression = request.getExpression();
        double result = evaluateExpression(expression);
        CalculationResult calculationResult = new CalculationResult();
        calculationResult.setResult(result);
        saveHistory(result);
        return calculationResult;
    }
    public void saveHistory(double result) {
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM history", Integer.class);
        if (count >= 10) {
            jdbcTemplate.update("DELETE FROM history ORDER BY timestamp LIMIT 1");
        }
        jdbcTemplate.update("INSERT INTO history (result) VALUES (?)", result);
    }
    @RequestMapping("/history")
    public List<Double> getHistory() {
        System.out.println("read history successfully");
        return jdbcTemplate.queryForList("SELECT result FROM history ORDER BY id DESC LIMIT 10", Double.class);
    }

    //计算
    public double evaluateExpression(String expression) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("js");
            Object result = engine.eval(expression);
            if (result instanceof Number) {
                BigDecimal bd = new BigDecimal(((Number) result).doubleValue()).setScale(7, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else {
                throw new RuntimeException("Invalid expression result");
            }
        } catch (ScriptException | javax.script.ScriptException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to evaluate expression: " + expression, e);
        }
    }

    //接收通过mqtt请求接收表达
    public void calculateExpression (String topic) throws MqttException {
        mqttClient.subscribe(topic, (topic1, message) -> {
            String expression = request.getExpression();
            double result = evaluateExpression(expression);
            saveHistory(result);
        });
    }

    //返回前端
    public void subscribeAudio(String topic) throws MqttException {
        mqttClient.subscribe(topic, (topic2, message) -> {
            MqttMessage mqttMessage = new MqttMessage();
            String test= String.valueOf(jdbcTemplate.queryForList("SELECT result FROM history ORDER BY id DESC LIMIT 10", Double.class);
);
            mqttMessage.setPayload(test.getBytes(StandardCharsets.UTF_8));
            mqttClient.publish("frontend",mqttMessage);
        });
    }
}