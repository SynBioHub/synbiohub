
const prepareDisplay = require('visbol').prepareDisplay;
const React = require('react');
const ReactDOM = require('react-dom');
import Rendering from 'visbol-react';

if(document.getElementById('visboldesign')
    && typeof meta !== 'undefined'
    && meta.displayList) {

    const container = document.getElementById('visboldesign');
    if (typeof window !== 'undefined') {
        const display = prepareDisplay(meta.displayList);
        ReactDOM.render(<Rendering display={display} toLog={meta.displayList}/>, container);
    }
}
