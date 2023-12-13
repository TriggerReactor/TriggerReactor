interface SidebarItemProps {
    contents: string
    imgPath: string
    onClick: () => void
    showDetails: boolean
}


type SidebarItemType = {
    contents: string
    imgPath: string
    onClick: () => void
}

type SideBarListType = {
    index: int
    itemGroup: SidebarItemType[]
}