import { useState } from 'react'

import './Sidebar.css'
import { sidebarItems } from './SidebarItemsData';
import BlockPanel from '../BlockManager/BlockPanel/BlockPanel'

const SidebarItem = ({contents, imgPath, onClick, showDetails}: SidebarItemPropsType) => {
    return (
        <li className='sidemenu-item' onClick={onClick}>
            <img className='sidemenu-icon' src={imgPath} alt="" />
            <p className={'sidemenu-text ' + (showDetails ? 'sidemenu-text-open' : 'sidemenu-text-close')}>{contents}</p>
        </li>
    )
}

function Sidebar() {
    const [showDetails, setShowDetails] = useState(false);

    const handleMouseEnter = () => {
        setShowDetails(true);
    }

    const handleMouseExit = () => {
        setShowDetails(false);
    }

    return (
        <div className="sidemenu">
            <BlockPanel />
            <div className={"sidemenu-shadow-bg " + (showDetails ? 'sidemenu-shadow-bg-show' : 'sidemenu-shadow-bg-hide')}></div>
            <div className="sidemenu-nav" onMouseOver={handleMouseEnter} onMouseOut={handleMouseExit}>
                <div className="sidemenu-selector"></div>
                {sidebarItems.map((itemGroup, index) => (
                    <div key={index}>
                        <ul className='sidemenu-list'>
                            {itemGroup.map((item, itemIndex) => (
                                <SidebarItem
                                    key={itemIndex}
                                    contents={item.contents}
                                    imgPath={item.imgPath}
                                    onClick={item.onClick}
                                    showDetails={showDetails}
                                />
                            ))}
                        </ul>
                        {index < sidebarItems.length - 1 && <hr className='sidemenu-divider' />} {/* 마지막 요소에는 구분선(hr) 추가하지 않음 */}
                    </div>
                ))}
            </div>
        </div>
    )
}

export default Sidebar;