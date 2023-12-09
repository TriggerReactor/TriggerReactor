export const imgRoot = "./imgs/icons/side_menu/";

export const sidebarItems: SidebarItemType[][] = [
    [
        { contents: "Triggers", imgPath: (imgRoot+"triggers.svg"), onClick:() => {} },
        { contents: "Events", imgPath: (imgRoot+"events.svg"), onClick:() => {} },
        { contents: "Imports", imgPath: (imgRoot+"imports.svg"), onClick:() => {} }
    ],
    [
        { contents: "Executors", imgPath: (imgRoot+"executors.svg"), onClick:() => {} },
        { contents: "Placeholders", imgPath: (imgRoot+"placeholders.svg"), onClick:() => {} },
        { contents: "Conditions", imgPath: (imgRoot+"conditions.svg"), onClick:() => {} },
        { contents: "Variables", imgPath: (imgRoot+"variables.svg"), onClick:() => {} },
        { contents: "Methods", imgPath: (imgRoot+"methods.svg"), onClick:() => {} }
    ]
]