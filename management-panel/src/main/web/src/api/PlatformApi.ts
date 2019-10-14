import {
    ADD_PROJECT,
    AUTHENTICATE,
    AUTHORIZE,
    DELETE_PROJECT,
    GET_PROJECTS,
    REGISTER_USER
} from "../constants/Constants";
import {createMethodRequest, fireAndForget, requestResponse} from "./PlatformClient";

export const registerUser = (requestData: UserRegistrationRequest, onComplete: (user: UserRegistrationResponse) => void) => {
    requestResponse(createMethodRequest(REGISTER_USER, requestData))
        .then(onComplete);
};

export const authorize = (requestData: UserAuthorizationRequest, onComplete: (user: UserAuthorizationResponse) => void, onError: () => void) => {
    requestResponse(createMethodRequest(AUTHORIZE, requestData))
        .then(onComplete)
        .catch(onError);
};

export const authenticate = (requestData: string, onComplete: (authorized: boolean) => void, onError: () => void) => {
    requestResponse(createMethodRequest(AUTHENTICATE, requestData))
        .then(onComplete)
        .catch(onError)
};

export const addProject = (requestData: ProjectRequest, onComplete: (project: Project) => void, onError: () => void) => {
    requestResponse(createMethodRequest(ADD_PROJECT, requestData))
        .then(onComplete)
        .catch(onError)
};

export const deleteProject = (requestData: number) => {
    fireAndForget(createMethodRequest(DELETE_PROJECT, requestData))
};

export const getProjects = (onComplete: (projects: Map<number, Project>) => void, onError: () => void) => {
    requestResponse(createMethodRequest(GET_PROJECTS))
        .then((projects: Project[]) => onComplete(projects.groupByIgnoreDuplicates(project => project.id)))
        .catch(console.error)
        .catch(onError)
};