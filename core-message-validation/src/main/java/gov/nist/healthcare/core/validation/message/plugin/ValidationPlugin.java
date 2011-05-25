package gov.nist.healthcare.core.validation.message.plugin;

import gov.nist.healthcare.core.message.HL7Message;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.util.TableManager;
import gov.nist.healthcare.validation.AssertionResultConstants;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class ValidationPlugin {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected TableManager tableManager;

    public void setTableManager(TableManager tableManager) {
        this.tableManager = tableManager;
    }

    public abstract List<MessageFailure> validate(HL7Message message,
            String params, AssertionResultConstants.Enum assertionResult,
            String userComment) throws Exception;

}
