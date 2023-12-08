type SidebarItemType = {
    contents: string
    imgPath: string
    onClick: () => void
}

type SidebarItemPropsType = {
    contents: string
    imgPath: string
    onClick: () => void
    showDetails: boolean
}

type SideBarListType = {
    index: int
    itemGroup: SidebarItemType[]
}