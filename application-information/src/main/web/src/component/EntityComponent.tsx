/*
 * ART Java
 *
 * Copyright 2019 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, {useMemo} from "react";
import {Chip, ExpansionPanel, ExpansionPanelDetails, ExpansionPanelSummary, Typography, useTheme} from "@material-ui/core";
import ExpandMore from "@material-ui/icons/ExpandMore";
import ReactJson from "react-json-view";

interface EntityComponentProps {
    type: EntityType
    entity: string
}

export enum EntityType {
    REQUEST,
    RESPONSE
}

export const EntityComponent = (props: EntityComponentProps) => {
    const theme = useTheme();
    const entity = useMemo(() => {
        const entityJson = JSON.parse(props.entity);
        if (typeof entityJson != "object") {
            return props.entity
        }
        return <ReactJson style={{fontSize: theme.typography.h6.fontSize}}
                          displayObjectSize={false}
                          displayDataTypes={false}
                          name={props.type == EntityType.REQUEST ? "request" : "response"}
                          src={entityJson}/>
    }, [props.entity]);
    return <ExpansionPanel>
        <ExpansionPanelSummary expandIcon={<ExpandMore/>}>
            <Chip color={"primary"}
                  label={<Typography>{props.type == EntityType.REQUEST ? "Пример запроса" : "Пример ответа"}</Typography>}/>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
            {entity}
        </ExpansionPanelDetails>
    </ExpansionPanel>;
};
