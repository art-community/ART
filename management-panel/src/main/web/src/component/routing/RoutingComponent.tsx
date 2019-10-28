import * as React from "react";
import {useEffect, useState} from "react";
import {BrowserRouter, Route} from 'react-router-dom';
import {AUTHORIZE_PATH, BUILD_PATH, DEPLOY_PATH, PROJECT_PATH, SLASH, TOKEN_COOKIE} from "../../constants/Constants";
import {ThemeProvider} from "@material-ui/styles";
import {CssBaseline} from "@material-ui/core";
import {DEFAULT_THEME} from "../../theme/Theme";
import {Redirect, Switch} from "react-router";
import {ProjectsComponent} from "../project/ProjectsComponent";
import {BuildComponent} from "../build/BuildComponent";
import {AuthorizationComponent} from "../authorization/AuthorizationComponent";
import {SideBarComponent} from "../sidebar/SideBarComponent";
import {DeployComponent} from "../deploy/DeployComponent";
import Cookies from "js-cookie";
import {authenticate} from "../../api/PlatformApi";
import {History} from "history"

export const RoutingComponent = () => {
    const [authorized, setAuthorized] = useState(true);
    const [referer, setReferer] = useState(PROJECT_PATH);
    useEffect(() => authenticate(Cookies.get(TOKEN_COOKIE) as string,
        () => setAuthorized(true),
        () => setAuthorized(false)),
        []);

    const routePrivateComponent = (path: string, component: any) => {
        setReferer(path);
        if (Cookies.get(TOKEN_COOKIE) && authorized) {
            return component
        }
        return <Redirect to={AUTHORIZE_PATH}/>
    };

    const routePublicComponent = (component: any) => {
        if (Cookies.get(TOKEN_COOKIE) && authorized) {
            return <Redirect to={referer}/>
        }
        return component
    };

    const handleAuthorized = (history: History, token: string) => {
        Cookies.set(TOKEN_COOKIE, token);
        setAuthorized(true);
        history.push(referer)
    };

    const decorateWithSideBar = (component: React.ReactNode) => <SideBarComponent>{component}</SideBarComponent>;

    return <ThemeProvider theme={DEFAULT_THEME}>
        <CssBaseline/>
        <BrowserRouter>
            <Switch>
                <Route exact path={BUILD_PATH}>
                    {() => routePrivateComponent(BUILD_PATH, decorateWithSideBar(<BuildComponent/>))}
                </Route>
                <Route exact path={PROJECT_PATH}>
                    {() => routePrivateComponent(PROJECT_PATH, decorateWithSideBar(<ProjectsComponent/>))}
                </Route>
                <Route exact path={DEPLOY_PATH}>
                    {() => routePrivateComponent(DEPLOY_PATH, decorateWithSideBar(<DeployComponent/>))}
                </Route>
                <Route exact path={AUTHORIZE_PATH}>
                    {() => routePublicComponent(<AuthorizationComponent onAuthorize={handleAuthorized}/>)}
                </Route>
                <Route path={SLASH}>
                    <Redirect to={PROJECT_PATH}/>
                </Route>
            </Switch>
        </BrowserRouter>
    </ThemeProvider>
};