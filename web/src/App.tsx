import React from 'react';

import Header from './components/Header/Header'

import './App.css';
import Sidebar from './components/Sidebar/Sidebar';

function App() {
    return (
        <div className="app">
            <header className="globalheader">
                <Header />
            </header>
            <Sidebar />
        </div>
    );
}

export default App;
