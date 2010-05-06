drop table settings if exists;

create table settings (
    id INTEGER IDENTITY,
    key VARCHAR(100) unique NOT NULL,
    value VARCHAR(2000),
    type VARCHAR(30)
);

insert into settings (key, value, type) values
    ('gui.mainWindowState', '6', 'INTEGER'),
    ('gui.controlPanelBg',
        '<root>
            <field name="r" value="238" type="int"/>
            <field name="g" value="238" type="int"/>
            <field name="b" value="238" type="int"/>
         </root>', 'COLOR'),
    ('gui.mainWindowPosition',
        '<root>
            <field name="x" value="0" type="int"/>
            <field name="y" value="0" type="int"/>
            <field name="width" value="640" type="int"/>
            <field name="height" value="480" type="int"/>
         </root>', 'RECTANGLE'),
    ('gui.playlistFont',
        '<root>
            <field name="name" value="Sans Serif" type="string"/>
            <field name="style" value="0" type="int"/>
            <field name="size" value="14" type="int"/>
         </root>', 'FONT'),
    ('gui.selectionColor',
        '<root>
            <field name="r" value="47" type="int"/>
            <field name="g" value="96" type="int"/>
            <field name="b" value="149" type="int"/>
         </root>', 'COLOR'),
    ('gui.backgroundColor',
        '<root>
            <field name="r" value="5" type="int"/>
            <field name="g" value="53" type="int"/>
            <field name="b" value="92" type="int"/>
         </root>', 'COLOR'),
    ('gui.testColor',
        '<root>
            <field name="r" value="255" type="int"/>
            <field name="g" value="255" type="int"/>
            <field name="b" value="244" type="int"/>
         </root>', 'COLOR'),
    ('gui.highlightColor',
        '<root>
            <field name="r" value="166" type="int"/>
            <field name="g" value="215" type="int"/>
            <field name="b" value="255" type="int"/>
         </root>', 'COLOR'),
    ('gui.LAF', 'com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel', 'STRING'),

    ('tagging.defaultEncoding', 'windows-1251', 'STRING'),

    ('codecs.mp3.useNative', 'false', 'BOOLEAN'),
    ('array',
        '<root>
            <field name="0" value="zero" type="string"/>
            <field name="1" value="one" type="string"/>
            <field name="2" value="two" type="string"/>
         </root>', 'ARRAY'),
    ('playlistColumns',
        '<root>
            <field name="Playing" value="0" type="int"/>
            <field name="Name" value="[%artist% - ]$if3(%title%, %fileName%)" type="string"/>
            <field name="Length" value="%length%" type="string"/>
            <field name="Album" value="%album%" type="string"/>
            <field name="Date" value="%year%" type="string"/>
         </root>', 'MAP');