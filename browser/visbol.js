
const prepareDisplay = require('visbol').prepareDisplay;
const React = require('react');
const ReactDOM = require('react-dom');
import Rendering from 'visbol-react';

if(document.getElementById('design')
    && typeof meta !== 'undefined'
    && meta.displayList) {

    const container = document.getElementById('design');
    if (typeof window !== 'undefined') {
        console.log(JSON.stringify(meta.displayList, null, 2));
        const display = prepareDisplay(meta.displayList);
        ReactDOM.render(<Rendering display={display}/>, container);
    }
}
