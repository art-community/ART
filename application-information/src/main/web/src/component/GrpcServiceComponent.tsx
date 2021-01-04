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

import React from "react";
import {
    Box,
    Chip,
    createStyles,
    ExpansionPanel,
    ExpansionPanelDetails,
    ExpansionPanelSummary,
    Grid,
    makeStyles,
    Theme,
    Typography
} from "@material-ui/core";

import ExpandMore from "@material-ui/icons/ExpandMore";
import {GRPC_CHIP_STYLE} from "../constants/Constants";
import {GrpcMethodComponent} from "./GrpcMethodComponent";
import {GrpcInformation, GrpcServiceInformation, GrpcServiceMethodInformation} from "../model/Models";

interface GrpcServiceComponentProps {
    information: GrpcInformation,
    serviceInformation: GrpcServiceInformation
}

const useStyles = makeStyles(() => createStyles({
    chip: GRPC_CHIP_STYLE
}));

export const GrpServiceComponent = (props: GrpcServiceComponentProps) => {
    const styles = useStyles();
    const grpcMethods = () => Array.from(new Map<string, GrpcServiceMethodInformation>(Object.entries(props.serviceInformation.methods)).values());

    return <ExpansionPanel>
        <ExpansionPanelSummary expandIcon={<ExpandMore/>}>
            <Chip variant={"outlined"} className={styles.chip}
                  label={<Typography>GRPC</Typography>}/>
            <Box ml={1}>
                <Typography variant={"h6"} color={"secondary"}>
                    Сервис {props.serviceInformation.id}
                </Typography>
            </Box>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
            <Grid container direction={"column"} spacing={1}>
                {props.information.url
                    ? <Grid container item>
                        <Chip variant={"outlined"} className={styles.chip}
                              label={<Typography>URL</Typography>}/>
                        <Box ml={1}>
                            <Typography variant={"subtitle1"} color={"secondary"}>
                                {props.information.url}
                            </Typography>
                        </Box>
                    </Grid>
                    : <></>
                }
                {grpcMethods().map(method =>
                    <Grid key={method.id} item>
                        <GrpcMethodComponent information={method}/>
                    </Grid>)}
            </Grid>
        </ExpansionPanelDetails>
    </ExpansionPanel>;
};
