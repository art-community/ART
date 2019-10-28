import * as React from "react";
import {useState} from "react";
import {SelectProjectComponent} from "./SelectProjectComponent";
import {AssembliesComponent} from "./AssembliesComponent";
import {Project} from "../../model/Models";

export const BuildComponent = () => {
    const [selectedProject, selectProject] = useState<Project | null>(null);
    return selectedProject
        ? <AssembliesComponent project={selectedProject} onBack={() => selectProject(null)}/>
        : <SelectProjectComponent onSelect={selectProject}/>;
};