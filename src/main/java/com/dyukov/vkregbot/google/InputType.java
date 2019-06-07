package com.dyukov.vkregbot.google;

import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.config.AppProperties;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.view.DataRetrievable;

public enum InputType {

    INPUT("input") {
        @Override
        public KeyValue getValue(Element input, DataRetrievable registerData) throws ServiceException {
            logger.info("Handling input type input...");
            String label = input.attr("aria-label").toLowerCase();
            if (label.contains(getProperty("team.label")) || label.contains(getProperty("team.label.2"))) {
                logger.info("It is a team name input.");
                return getTeamNameKey(input, registerData);
            }
            if (label.contains(getProperty("link.label")) || label.contains("vk.link.label")) {
                logger.info("It is a profile link input");
                return getProfileLink(input, registerData);
            }
            handleUnknownInput(input);
            return null;
        }
    },
    TEXTAREA("textarea") {
        @Override
        public KeyValue getValue(Element input, DataRetrievable registerData) {
            logger.info("Handling input type textarea...");
            handleUnknownInput(input);
            return null;
        }
    },
    RADIOGROUP("radiogroup") {
        @Override
        public KeyValue getValue(Element input, DataRetrievable registerData) throws ServiceException {
            logger.info("Handling input type radiogroup...");
            String title = input.parent().parent().getElementsByClass("exportItemTitle").get(0).text().toLowerCase();
            if (title.contains(getProperty("how.much.you.label"))) {
                logger.info("It is a team members count input.");
                return new KeyValue(input.attr("name"), String.valueOf(registerData.getTeamMembersCount()));

            }
            handleUnknownInput(input);
            return null;
        }
    },
    LISTBOX("listbox") {
        @Override
        public KeyValue getValue(Element input, DataRetrievable registerData) {
            logger.info("Handling input type listbox...");
            handleUnknownInput(input);
            return null;
        }
    },
    LISTITEM("listitem") {
        @Override
        public KeyValue getValue(Element input, DataRetrievable registerData) throws ServiceException {
            logger.info("Handling input type listbox...");
            String label = input.previousElementSibling().getElementsByClass("exportItemTitle").text().toLowerCase();
            if (label.contains(getProperty("when.label")) || label.contains(getProperty("play.label"))) {
                logger.info("It is a date input.");
                Element container = input.nextElementSibling();
                Elements spans = container.getElementsByTag("span");
                Elements inputs = container.getElementsByTag("input");
                List<String> values = spans.stream()
                        .map(Element::text)
                        .collect(Collectors.toList());
                List<String> inputNames = inputs.stream()
                        .map(inp -> inp.attr("name"))
                        .collect(Collectors.toList());
                int dayNumber = registerData.getDayNumber() - 1;

                return new KeyValue(inputNames.get(dayNumber), values.get(dayNumber));
            }
            handleUnknownInput(input);
            return null;
        }
    };

    private static KeyValue getProfileLink(Element input, DataRetrievable registerData) {
        return getNameKeyValue(input, registerData.getVkProfileLink());
    }

    private static KeyValue getTeamNameKey(Element input, DataRetrievable registerData) {
        return getNameKeyValue(input, registerData.getTeamName());
    }

    private String typeName;
    private static Logger logger = LoggerFactory.getLogger(InputType.class);

    InputType(String typeName) {
        this.typeName = typeName;
    }

    static InputType fromInputType(String inputType) throws ServiceException {
        for (InputType currentType : InputType.values()) {
            if (inputType.equals(currentType.typeName))
                return currentType;
        }
        throw new ServiceException("Unknown input type: " + inputType);
    }

    protected static String getProperty(String key) throws ServiceException {
        return AppProperties.getInstance().getProperty(key);
    }

    protected void handleUnknownInput(Element input) {
        logger.info("It is unknown input");
        logger.info(input.html());
    }

    protected static KeyValue getNameKeyValue(Element input, String value) {
        return new KeyValue(input.attr("name"), value);
    }

    public abstract KeyValue getValue(Element input, DataRetrievable registerData) throws ServiceException;
}
