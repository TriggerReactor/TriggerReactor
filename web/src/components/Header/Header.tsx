import './Header.css'
import { headerItems } from './HeaderItemsData'

function HeaderItem({ name, imgPath, onClick, isIconOnly }: HeaderItemType ) {
    return (
        <li className='header-item' onClick={()=> onClick()}>
            <img className='header-icon' src={imgPath} alt={name} />
            {isIconOnly ? null : <p className='header-text'>{name}</p>}
        </li>
    );
}   


function Header() {

    const renderHeaderItems = (items: HeaderItemType[], filter: (item: HeaderItemType) => boolean) => (
        items
            .filter(filter)
            .map((item, index) => (
                <HeaderItem  
                    key={index}
                    name={item.name}
                    imgPath={item.imgPath}
                    onClick={item.onClick}
                    isIconOnly={item.isIconOnly}
                />
            ))
    );

    return(
        <nav className='header-nav'>
            <ul className='header-list'>
                <img src={ process.env.PUBLIC_URL + "/imgs/icons/logo.svg" } alt="logo" className="header-logo"/>
                {renderHeaderItems(headerItems, item => !item.isIconOnly)}
            </ul>
            <ul className='header-list'>
                {renderHeaderItems(headerItems, item => item.isIconOnly)}
            </ul>
        </nav>
    );
}


export default Header;