<?xml version="1.0" encoding="UTF-8"?>

<config charset="ISO-8859-1">
    
    <include path="functions.xml"/>
                
    <!-- collects all tables for individual products -->
    <var-def name="products">    
        <call name="download-multipage-list">
            <call-param name="pageUrl">http://shopping.yahoo.com/s:Digital%20Cameras:4168-Brand=Canon:browsename=Canon%20Digital%20Cameras:refspaceid=96303108;_ylt=AnHw0Qy0K6smBU.hHvYhlUO8cDMB;_ylu=X3oDMTBrcDE0a28wBF9zAzk2MzAzMTA4BHNlYwNibmF2</call-param>
            <call-param name="nextXPath">//a[.//span[.='Next']]/@href</call-param>
            <call-param name="itemXPath">//table[@class="item_table"]/tbody/tr</call-param>
            <call-param name="maxloops">10</call-param>
        </call>
    </var-def>
    
    <!-- iterates over all collected products and extract desired data -->
    <file action="write" path="canon/catalog.xml" charset="UTF-8">
        <![CDATA[ <catalog> ]]>
        <loop item="item" index="i">
            <list><var name="products"/></list>
            <body>
                <xquery>
                    <xq-param name="item" type="node()"><var name="item"/></xq-param>
                    <xq-expression><![CDATA[
                            declare variable $item as node() external;

                            let $name := data($item//td[3]/h2[1])
                            let $desc := data($item//td[3]/div[@class='desc'])
                            let $price := data($item//div[@class='price']/a[1])
                                return
                                    <product>
                                        <name>{normalize-space($name)}</name>
                                        <desc>{normalize-space($desc)}</desc>
                                        <price>{normalize-space($price)}</price>
                                    </product>
                    ]]></xq-expression>
                </xquery>
            </body>
        </loop>
        <![CDATA[ </catalog> ]]>
    </file>

</config>