import React from 'react';

import './App.css';
import Sidebar from './components/Sidebar/Sidebar';
import Header from './components/Header/Header';
import Grid from './components/Grid/Grid';

import DragTest from './test/DragTest';

function App() {
    return (
        <div className="app">
            <header className="globalheader">
                <Header />
            </header>
            <Sidebar />
            <Grid />
            {/* <DragTest /> */}
        </div>
    );
}

export default App;
