import { TestBlockList } from '../BlockList/BlockManager'

import './BlockPanel.css'

function BlockTitle() {
    return (
        <div className="block-info">
            <p className="block-cotroller-title">Triggers</p>
            <input className="search" type="text" placeholder="Search" />
            <hr className='block-divider'/>
        </div>
    )
}

function BlockPanel() {
    
    return (
        <div className='block-cotroller'>
            <BlockTitle />
            <TestBlockList />
        </div>
    )
}

export default BlockPanel;